alter table actiontakens add column createdon timestamp null default null;
update actiontakens set createdon = `time` where id <> -1; -- -1 for safe update mode
