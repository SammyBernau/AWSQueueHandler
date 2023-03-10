#How to Run
1) Run the postgresql and localstack containers and make sure each are set to the 5432 and 4566 respectively. 
2) Copy the AWSQueueHandler repository 
3) Setup the database in your code editor of choice and make sure you can connect to the postgresql database 
   * User = postgres
   * Password = postgres
4) When connection is secure, go to the 'Main' method and run the program
5) Check in the postgresql console for the updated database

**Important note, for the sake of this project value casting was used to alter the 'app_version' column data type from integer to varchar(32) so there is more work to be done to fully flush out the program so that data types match exactly**



#Questions

###How would you deploy this application in production?
  * I would deploy this application by installing the program as a jar on a server and having it connect to the required database/systems
    * There is more security precaution work that must be done before this, such as safely storing (and encrypting) the HashTables that store the hashed valued and their original text
      * This could potentially be done via another database with limited access or a secured file system
  * Another way this could be deployed is by making this program into a container itself and running it alongside the other two
    * This way there is no issue in terms of environment variables and dependencies
  
###What other components would you want to add to make this production ready?
  * As mentioned above, more security features would be the first items to implement as the data collection is meaningless if a breach occurs and customer information is lost or stolen.
  * Other items such as enabling the rest of the other sql operators to provide better tools for analysts when looking at the data
    * ie. cubeby, where, select, groupby etc.
  
###How can this application scale with a growing dataset.
* This application can scale with many things but the first that comes to my mind is threads. 
  * With different threads the messages could be handled at a greater volume and more efficiently
* The application would also benefit from a larger pool of memory and/or cpu power to facilitate the influx of Messages
  
###How can PII be recovered later on?
  * PII can be recovered via the HashTables. Whoever may need the information just needs to look in the postgresql database, select a masked value and use the associated HashMap to recover the PII
    * HashMaps are perfect for this kind of look up as they are extremely fast and easily extended
  
###What are the assumptions you made?
* The largest assumption that I made was casting the 'app_version' data type from integer to varchar(32)
  * The reason for this was to make it easier for storing the app_version values; however, the biggest caveat to this is that if there is an existing table, casting the data type is not a solution. A potential solution to this is to copy the already existing table, change the specified column and then delete the old table. This may waste time and memory so another solution should be found for production.
* Another assumption I made at first were the way AWS SQS Messages were handled. Initially, I thought you could pull all messages and parse them; however, I discovered that Queues cannot be treated this way. 
  * Thus, my fix for this was to use an infinite loop with a long poll to check for messages; although this too could cost memory over time. 
  * If I had more time, this would be one of the first updates I would make to improve stability and performance of the application