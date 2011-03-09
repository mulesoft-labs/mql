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
	        <mql:transform query="from payload as p where division = 'Sales'
	                                select new { name = firstName + ' ' + lastName }" />
	    </flow>
	
	</mule>
	