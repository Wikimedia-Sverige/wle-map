Depends on JWBF:

```
git clone git@github.com:eldur/jwbf.git
cd jwbf
mvn install -Dtest=false -DfailIfNoTests=false
```

Start API server on port 8080 by executing `mvn spring-boot:run -Dspring-boot.run.jvmArguments="-DCONSTRETTO_TAGS=dev,fully_cached"` from the root.

`CONSTRETTO_TAGS` denotes environmental settings available in `src/main/resources/service.ini`

`dev` use paths in the project target/ directory

`prod` use paths for installation on `sites.wikimedia.se`.

For high CPU and low RAM, use `no_cache`. This will allow for 5 concurrent users on a single core with a mean response time of 800ms.

For low CPU and high RAM, use `fully_cached`. This will allow for several hundred concurrent users on a single core with a mean response time of 10ms.
