Mashup Query Language
=====================

Using the Query Language
------------------------
This project is a LINQ like mechanism to query and transform data. Excuting 
queries is simple:

	// populate some data
	List<Person> persons = new ArrayList<Person>();
	persons.add(new Person("Dan", "Diephouse", "MuleSoft", "Engineering"));
	persons.add(new Person("Joe", "Sales", "MuleSoft", "Sales"));
	
	// create a context for the query
	Map<String,Object> context = new HashMap<String,Object>();
	
	// execute the query
	Collection<Person> result = 
	    Query.execute("from persons as p where p.division = 'Engineering'", context);
	  
	assertEquals(1, result.size()); // the result will just contain the first person

The query syntax is best illustrated by example queries below.

Filtering Collections:

	from people as p where p.division = 'Sales'
	from people as p where p.division = 'Sales' and p.firstName = 'Dan'

Ordering collections:

	from people as p order by name

Transforming a collection into new objects:

	from people as p where p.division = 'Sales' 
	  select new {
	    href = 'http://localhost/sales/people/' + p.id,
	    name = p.firstName + ' ' + p.lastName,
	    division = p.division
	  }
  
This will create a new set of objects with href and name properties. The href 
property will be a combination of the string url and the object id. The name
property will be a synthesis of the first and last name.

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
	        <inbound-endpoint address="vm://test"
	            exchange-pattern="request-response" />
	        <mql:transform query="from payload as p select new { name = p.firstName }" />
	    </flow>
	
	</mule>