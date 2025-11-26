###################sensor assignment######################

use instructions:
1.you should clone the repo to your intelij
2.build and compile the project ;
3.run the server on local
client server interaction :
1.use postman:
2.add attached collection to postman
3.add attached envoirment to have env veraibles 
4.run create user&role with role USER->you have the abilety only to create role_user
5.run authenticate with name and password of created user
6.now you can run any of the meanrioned endpoints:upload csv operation
get resaults filtered by device or by id or both or summery of resault as meantioned in swagger
also endpoints related tojobs and status,and even try validation scenarios as error and empty upload.
7.if you like to get system status or helth check you need to run authenticate again with 
admin user i created for statistics:
    "role": "ADMIN",
    "name": "alex",
    "pass":"bobikos"
8. there is an attached sample file in the project that you can upload, and another attached file with currupted input 

