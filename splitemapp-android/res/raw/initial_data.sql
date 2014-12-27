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

-- Data for group_status table
insert into group_status(id,cod,title) values(1,"active","Activo");
insert into group_status(id,cod,title) values(2,"disabled","Desactivado");
insert into group_status(id,cod,title) values(3,"deleted","Borrado");

-- Data for user_to_group_status table
insert into user_to_group_status(id,cod,title) values(1,"active","Activo");
insert into user_to_group_status(id,cod,title) values(2,"removed","Eliminado");
insert into user_to_group_status(id,cod,title) values(3,"left_group","Dejo el grupo");

-- Data for user_to_project_status table
insert into user_to_project_status(id,cod,title) values(1,"active","Activo");
insert into user_to_project_status(id,cod,title) values(2,"removed","Eliminado");
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
insert into sync_status(id,table_name) values(1,"group");
insert into sync_status(id,table_name) values(2,"project");
insert into sync_status(id,table_name) values(3,"user");
insert into sync_status(id,table_name) values(4,"user_contact_data");
insert into sync_status(id,table_name) values(5,"user_expense");
insert into sync_status(id,table_name) values(6,"user_invite");
insert into sync_status(id,table_name) values(7,"user_to_group");
insert into sync_status(id,table_name) values(8,"user_to_project");