Mashup Query Language
=====================

MQL is a data query language that seeks to make it easy to do:
1) Filtering of complex data sets
2) Transformation of objects
3) Join data across disparate data sources

Using the Query Language
------------------------
Executing queries is simple:

	// populate some data
	List<Person> persons = new ArrayList<Person>();
	persons.add(new Person("Dan", "Diephouse", "MuleSoft", "Engineering"));
	persons.add(new Person("Joe", "Sales", "MuleSoft", "Sales"));
	
	// create a context for the query
	Map<String,Object> context = new HashMap<String,Object>();
	context.put("persons", persons);
	
	// execute the query
	Collection<Person> result = 
	    Query.execute("from people where division = 'Engineering'", context);
	  
	assertEquals(1, result.size()); // the result will just contain the first person

The query syntax is best illustrated by example queries below.

Filtering Collections:

	from people where division = 'Sales'
	from people as p where p.division = 'Sales' // explicit syntax
	from people where division = 'Sales' and (firstName = 'Dan' or firstName = 'Joe')
	from people where division = 'Sales' 

Querying objects in your query context:
   
    // execute the getPeople() method on the salesforce object
    from salesforce.people as p where p.lastName = 'Diephouse'
    // the more explicit syntax is also valid
    from salesforce.getPeople() as p where p.lastName = 'Diephouse'
    
Ordering collections:

	from people as p order by name

Transforming a collection into new objects:

    select new {
	  href = 'http://localhost/sales/people/' + id,
	  name = firstName + ' ' + lastName,
	  division = division
	}
	
	// This example shows the more explicit syntax
	from persons as p select new {
	  href = 'http://localhost/sales/people/' + p.id,
	  name = p.firstName + ' ' + p.lastName,
	  division = p.division
	}

These queries will create a new set of objects with href and name properties. 
The href property will be a combination of the string url and the object id. 
The name property will be a synthesis of the first and last name from the 
person object.

Joining a data source:

   from people as p 
     join twitter.getUserInfo(p.twitterId) as twitterInfo
     select new {
       name = firstName + ' ' + lastName,
       tweets = twitterInfo.totalTweets
     }

This query assumes that you have an object named "twitter" inside your 
query context which has a method called getUserInfo() which takes a twitter 
user id. It then joins the result of this method call into a new object 
called "twitterInfo" which can be used in the select statement.

Using MQL in Mule
=================

You can use the mql:transform element inside of Mule to do MQL transformations.
Here is a very simple flow:

	<?xml version="1.0" encoding="UTF-8"?>
	<mule xmlns="http://www.mulesoft.org/schema/mule/core" 
	    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	    xmlns:spring="http://www.springframework.org/schema/beans"
	    xmlns:mql="http://www.mulesoft.org/schema/mule/mql" 
	    xsi:schemaLocation="
	               http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-3.0.xsd
	               http://www.mulesoft.org/schema/mule/core http://www.mulesoft.org/schema/mule/core/3.1/mule.xsd
	               http://www.mulesoft.org/schema/mule/mql http://www.mulesoft.org/schema/mule/mql/3.1/mule-mql.xsd
	               ">
	
	    <flow name="mql">
	        <inbound-endpoint address="vm://select"
	            exchange-pattern="request-response" />
	        <mql:transform query="from payload where division = 'Sales'
	                                select new { name = firstName + ' ' + lastName }" />
	    </flow>
	
	</mule>

If you want, you can refer to properties inside the Mule Message inside the 
where or select statements. In this example, the property 'someData' is 
added to the message and then added to the 'data' field of the object that
is created via the select statement.

    <flow name="selectDataFromMessageProperty">
        <inbound-endpoint address="vm://join"
            exchange-pattern="request-response" 
        <message-properties-transformer scope="invocation">
            <add-message-property key="someData" value="1000"/>
        </message-properties-transformer>/>
        <mql:transform 
            query="from payload
                     join mule.send('vm://twitter', p.twitterId) as twitterInfo
                     select new { 
                        name = firstName + ' ' + lastName, 
                        data = someData 
                     }" />
    </flow>

You can also join data from cloud connectors or other beans inside your Mule 
configuration file. For example, let's say you define a twitter cloud connector:

(WARNING: I haven't verified this syntax for this connector, but the idea
itself is sound and should work)

    <twitter:config name="twitter" ... />
    
You can then join it in using the bean name:

    <mql:transform 
        query="from payload as p 
                 join twitter.getUserInfo(p.twitterId) as twitterInfo
                 select new { 
                    name = firstName + ' ' + lastName, 
                    tweets = twitterInfo.totalTweets 
                 }" />

You can also join data from other endpoints. For example, if you had an endpoint on
vm://twitter which retreived information about a user, you could join it in via the 
mule query context variable and the send method.	

    <flow name="join">
        <inbound-endpoint address="vm://join"
            exchange-pattern="request-response" />
        <mql:transform 
            query="from payload as p 
                     join mule.send('vm://twitter', p.twitterId) as twitterInfo
                     select new { 
                        name = firstName + ' ' + lastName, 
                        tweets = twitterInfo.totalTweets 
                     }" />
    </flow>

Implementing a custom Query Context
=================================
If you have other data sources that you want to be able to join from, use in
your where query, or in your select statement, you can support these by 
implementing a LazyQueryContext. E.g., you may want to query beans in your
Spring ApplicationContext. You can implement a SpringQueryContext like this:

	public class SpringQueryContext extends LazyQueryContext {
	    private ApplicationContext applicationContext;
	
	    public SpringQueryContext(ApplicationContext applicationContext) {
	        super();
	        this.applicationContext = applicationContext;
	    }
	
	    @Override
	    public Object load(String key) {
	        if (applicationContext.containsBean(key)) {
	            return applicationContext.getBean(key);
	        }
	        return null;
	    }    
	}

(NOTE: this file is included in the com.mulesoft.mql.spring package)

Let's say that you have a bean defined in your Spring context like this:

    <bean id="personDao" class="....PersonDAOImpl"/>
    
Now you can write queries against it:

	// create a context for the query
	ApplicationContext applicationContext = ...; // inject via ApplicationContextAware
	Map<String,Object> queryContext = new SpringQueryContext(applicationContext);
	
	// execute the query
	Collection<Person> result = 
	    Query.execute("from personDao.persons where division = 'Engineering'", queryContext);
	  
This will call getPeople() on your personManager bean. 

You could also use it to join data across different beans. E.g., this query
joins a list of divisions inside a company with a list of people in each
division.
  
    from divisionDao as division 
      join personDao.getPersonsForDivision(division.id) as peopleInDivision
	  select new {
	     name = division.name,
	     people = peopleInDivision
	  }
	  