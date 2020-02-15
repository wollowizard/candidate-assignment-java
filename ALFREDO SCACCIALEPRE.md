# Political and Postal communities in Switzerland

##  Findings
I believe there is a wrong assumption (or a challenge) regarding the data model . As explained in comments in the code, there is a 1:n relationship between zip code and political community number. For this reason, two methods and their related tests work "by chance".

1. `ch.aaap.assignment.model.Model.getDistrictByZipCode` which returns a `String` representing the district name should instead return a `Collection<String>` representing all the possible district names for that zip code. I have added such method and a simple test for it, please see `ch.aaap.assignment.model.Model.getAllDistrictsByZipCode`
For an example, check zip code 1008 which corresponds to the political communities GDENR 5589 (Prilly), GDENR 5591	(Renens) and GDENR 5585	(Jouxtens-Mézery), which in turn have district names "District de Lausanne and "District de l'Ouest lausannois".
2. Similarly to point 1, for `ch.aaap.assignment.model.Model.getLastUpdateByPostalCommunityName` we have an issue because we can't uniquely go from a postal community name to a political community. In this case, the bug is even more hard to find because in the provided set of data, all political communities corresponding to a given postal community name actually have the same last update date.
For this specific issue, I gave my interpretation and wrote the method so that it returns the most recent of all the last updates of the political communities related to the postal community.


##  Description of my solution
###  Chose not to store computed relationships
As can be seen from the git history, I gave a first solution which stored the relationships among objects in the objects themselves.
I then changed it to have all methods compute the relationships on the fly. This because the cardinality of the data is not huge and won't predictably grow much (we don't expect to ever have millions or billions of zip codes).
Having things computed on the fly eliminates possible issues with updates and data consistency.

I also imagined a real life scenario, where this application could be (part of ) a REST web service. For such scenario, it is often desirable to have a stateless service.
Furthermore, in case we actually want to optimize performance/reduce load, we could use a caching layer, which is very often a better solution than storing objects in memory explicitely.
(distributed, memory cap, TTL, can be cleaned when data changes... )

### Choice of data structures
I used a HashMap data structure for the political communities, as they are uniquely identified by a code (which is also the parameter we are using on the postal data set as a foreign key). This way, we have O(1) access to political communities by code.
I used a simple Collection (Set) for postal communities, as they do not have a primary key (PLZ4 and PLZZ, even combined, are not unique.)

### Interfaces
In the provided example, we have interfaces for Canton, District and Model. I believe the purpose of those is just to indicate the desired operations to implement.
In a real life scenario, we would possibly not need those interfaces. I anyway kept them and created Imp classes, just not to have too many changed lines in the PR, so that you can focus on the actual code.

### Format source code
I usually work with IntelliJ Idea with checkstyle plugin, but I usually also add a maven-checkstyle-plugin or similar as part of the build process, so that the build pipelines that are automatically triggered on push will fail and inform the developer in case of code format issues. Check the pom file.
I often also add Jacoco plugin to make sure that the test coverage is exhaustive, but I thought that the target of this excercise is not to write tests, so I skipped it.