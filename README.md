Requires Java (JDK) 17.

Depends on JWBF:
```
git clone git@github.com:eldur/jwbf.git
cd jwbf
mvn install -Dtest=false -DfailIfNoTests=false
```

Start API server on port 8080 by executing `./mvnw spring-boot:run` from the root.

You can also define system environments using one or several constretto profiles, e.g.
`./mvnw spring-boot:run -Dspring-boot.run.jvmArguments="-DCONSTRETTO_TAGS=prod,fully_cached"`.

See file `src/main/resources/service.ini` for documentation and configuring your own profiles.

`default` settings are using paths in the project target/ directory and no caches.

`prod` use paths for installation on `sites.wikimedia.se`.

For high CPU and low RAM, use `default`. All caches will be turned off. This will allow for 5 concurrent users on a single core (modern as of 2022) with a mean response time of 500ms.

For low CPU and high RAM, use `fully_cached`. This will allow for several hundred concurrent users on a single core (modern as of 2022) with a mean response time of 20ms.
