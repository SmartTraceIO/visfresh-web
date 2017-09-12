alter table actiontakens add column createdon timestamp null default null;
update actiontakens set createdon = `time` where createdon is NULL and id <> -1; -- -1 for safe update mode
