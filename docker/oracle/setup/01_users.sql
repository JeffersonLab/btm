alter session set container = XEPDB1;

ALTER SYSTEM SET db_create_file_dest = '/opt/oracle/oradata';

create tablespace BTM;

create user "BTM_OWNER" profile "DEFAULT" identified by "password" default tablespace "BTM" account unlock;

grant connect to BTM_OWNER;
grant unlimited tablespace to BTM_OWNER;

grant create view to BTM_OWNER;
grant create sequence to BTM_OWNER;
grant create table to BTM_OWNER;
grant create procedure to BTM_OWNER;
grant create type to BTM_OWNER;
grant create trigger to BTM_OWNER;