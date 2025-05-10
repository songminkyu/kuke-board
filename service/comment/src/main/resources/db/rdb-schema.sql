create table comment (
    comment_id bigint not null primary key,
    content varchar(3000) not null,
    article_id bigint not null,
    parent_comment_id bigint,
    writer_id bigint not null,
    deleted bool not null,
    created_at datetime not null
);

create index idx_article_id_parent_comment_id_comment_id on comment(article_id asc, parent_comment_id asc, comment_id asc);

create table comment_v2 (
    comment_id bigint not null primary key,
    content varchar(3000) not null,
    article_id bigint not null,
    writer_id bigint not null,
    path varchar(25) character set utf8mb4 collate utf8mb4_bin not null,
    deleted bool not null,
    created_at datetime not null
);

create unique index idx_article_id_path on comment_v2(article_id asc, path asc);

create table article_comment_count (
    article_id bigint not null primary key,
    comment_count bigint not null
);

create table outbox (
    outbox_id bigint not null primary key,
    shard_key bigint not null,
    event_type varchar(100) not null,
    payload varchar(5000) not null,
    created_at datetime not null
);

create index idx_shard_key_created_at on outbox(shard_key asc, created_at asc);