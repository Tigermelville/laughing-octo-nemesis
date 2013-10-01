In the scripts file you will find two scripts I used
for test contention on the service, test.bash and shorty.bash

Also in the scripts directory is the SQL used to create
the postgres database.  In my deploy I used a utf-8 database
so unicode URLs are supported.

There is a scala worksheet in the project directory showing
my scratchpad work



The rest endpoints are as follows:

GET     /count     # Returns a count of the number of unique urls in the database as a Json string
POST    /          # The request must have Content-Type application/json
                   # The body is a single element JSON array containing the url
                   # to be shortened as a string.  (See scripts/shorty.bash)
GET     /short-url # Issues a redirect to the actual url     



In addition, a GET to the root will present a modest GUI for the service
GET     /          # presents the GUI

Richard Melville
tiger@disc.org
