insert into model (id, name, model, soc, memory_mb, ethernet, release_year) values (1, 'Raspberry Pi', 'B', 'BCM2835', 256, TRUE, 2012);
insert into model (id, name, model, soc, memory_mb, ethernet, release_year) values (2, 'Raspberry Pi Zero', 'Zero', 'BCM2835', 512, FALSE, 2015);
insert into model (id, name, model, soc, memory_mb, ethernet, release_year) values (3, 'Raspberry Pi Zero', '2W', 'BCM2835', 512, FALSE, 2021);

insert into inventory (id, model_id, quantity) values (1, 1, 0);
insert into inventory (id, model_id, quantity) values (2, 2, 20);
insert into inventory (id, model_id, quantity) values (3, 3, 300);