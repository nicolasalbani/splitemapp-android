-- Data for user_status table
insert into user_status values(1,"active","Activo");
insert into user_status values(2,"disabled","Desactivado");
insert into user_status values(3,"deleted","Borrado");

-- Data for project_status table
insert into project_status values(1,"active","Activo");
insert into project_status values(2,"closed","Cerrado");

-- Data for project_type table
insert into project_type values(1,"monthly","Mensual");
insert into project_type values(2,"one_time","Solo una vez");

-- Data for group_status table
insert into group_status values(1,"active","Activo");
insert into group_status values(2,"disabled","Desactivado");
insert into group_status values(3,"deleted","Borrado");

-- Data for user_to_group_status table
insert into user_to_group_status values(1,"active","Activo");
insert into user_to_group_status values(2,"removed","Eliminado");
insert into user_to_group_status values(3,"left_group","Dejo el grupo");

-- Data for user_to_project_status table
insert into user_to_project_status values(1,"active","Activo");
insert into user_to_project_status values(2,"removed","Eliminado");
insert into user_to_project_status values(3,"left_project","Dejo el proyecto");

-- Data for invite_status table
insert into invite_status values(1,"sent","Enviada");
insert into invite_status values(2,"accepted","Aceptada");
insert into invite_status values(3,"rejected","Rechazada");

-- Data for expense_category table
insert into expense_category values(1,"car","Automovil");