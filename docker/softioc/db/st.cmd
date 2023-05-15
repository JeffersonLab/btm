dbLoadRecords("/db/softioc.db")
dbLoadRecords("/db/macros.db", "HALL=A")
dbLoadRecords("/db/macros.db", "HALL=B")
dbLoadRecords("/db/macros.db", "HALL=C")
dbLoadRecords("/db/macros.db", "HALL=D")

iocInit
iocRun