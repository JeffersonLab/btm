dbLoadRecords("/db/softioc.db")
dbLoadRecords("/db/macros.db", "HALL=A")
dbLoadRecords("/db/macros.db", "HALL=B")
dbLoadRecords("/db/macros.db", "HALL=C")
dbLoadRecords("/db/macros.db", "HALL=D")

dbLoadRecords("/db/macros2.db", "HALL=a")
dbLoadRecords("/db/macros2.db", "HALL=b")
dbLoadRecords("/db/macros2.db", "HALL=c")
dbLoadRecords("/db/macros2.db", "HALL=d")

iocInit
iocRun