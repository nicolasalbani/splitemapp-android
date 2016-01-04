-- Data for user_status table
insert into user_status(id,cod,title) values(1,"active","Activo");
insert into user_status(id,cod,title) values(2,"disabled","Desactivado");
insert into user_status(id,cod,title) values(3,"deleted","Borrado");

-- Data for project_status table
insert into project_status(id,cod,title) values(1,"active","Activo");
insert into project_status(id,cod,title) values(2,"closed","Cerrado");

-- Data for project_type table
insert into project_type(id,cod,title) values(1,"monthly","Mensual");
insert into project_type(id,cod,title) values(2,"one_time","Solo una vez");

-- Data for user_to_project_status table
insert into user_to_project_status(id,cod,title) values(1,"active","Activo");
insert into user_to_project_status(id,cod,title) values(2,"archived","Archivado");
insert into user_to_project_status(id,cod,title) values(3,"left_project","Dejo el proyecto");

-- Data for invite_status table
insert into invite_status(id,cod,title) values(1,"sent","Enviada");
insert into invite_status(id,cod,title) values(2,"accepted","Aceptada");
insert into invite_status(id,cod,title) values(3,"rejected","Rechazada");

-- Data for expense_category table
insert into expense_category(id,cod,title) values(1,"car","Automovil");
insert into expense_category(id,cod,title) values(2,"travel","Viajes");
insert into expense_category(id,cod,title) values(3,"food","Comida y Bebidas");
insert into expense_category(id,cod,title) values(4,"family","Familia y Personal");
insert into expense_category(id,cod,title) values(5,"bills","Cuentas");
insert into expense_category(id,cod,title) values(6,"entertainment","Entretenimiento");
insert into expense_category(id,cod,title) values(7,"home","Hogar");
insert into expense_category(id,cod,title) values(8,"utilities","Utilidades");
insert into expense_category(id,cod,title) values(9,"shopping","Shopping");

-- Data for sync_status table
insert into sync_status(id,table_name,last_pull_at,last_pull_success_at,last_push_at,last_push_success_at) values(1,"user_avatar",'1970-01-01 00:00:00.000000','1970-01-01 00:00:00.000000',null,null);
insert into sync_status(id,table_name,last_pull_at,last_pull_success_at,last_push_at,last_push_success_at) values(2,"project",'1970-01-01 00:00:00.000000','1970-01-01 00:00:00.000000',null,null);
insert into sync_status(id,table_name,last_pull_at,last_pull_success_at,last_push_at,last_push_success_at) values(3,"user",'1970-01-01 00:00:00.000000','1970-01-01 00:00:00.000000',null,null);
insert into sync_status(id,table_name,last_pull_at,last_pull_success_at,last_push_at,last_push_success_at) values(4,"user_contact_data",'1970-01-01 00:00:00.000000','1970-01-01 00:00:00.000000',null,null);
insert into sync_status(id,table_name,last_pull_at,last_pull_success_at,last_push_at,last_push_success_at) values(5,"user_expense",'1970-01-01 00:00:00.000000','1970-01-01 00:00:00.000000',null,null);
insert into sync_status(id,table_name,last_pull_at,last_pull_success_at,last_push_at,last_push_success_at) values(6,"user_invite",'1970-01-01 00:00:00.000000','1970-01-01 00:00:00.000000',null,null);
insert into sync_status(id,table_name,last_pull_at,last_pull_success_at,last_push_at,last_push_success_at) values(7,"project_cover_image",'1970-01-01 00:00:00.000000','1970-01-01 00:00:00.000000',null,null);
insert into sync_status(id,table_name,last_pull_at,last_pull_success_at,last_push_at,last_push_success_at) values(8,"user_to_project",'1970-01-01 00:00:00.000000','1970-01-01 00:00:00.000000',null,null);
insert into sync_status(id,table_name,last_pull_at,last_pull_success_at,last_push_at,last_push_success_at) values(9,"user_session",'1970-01-01 00:00:00.000000','1970-01-01 00:00:00.000000',null,null);