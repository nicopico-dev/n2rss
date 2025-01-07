/*
 * Copyright (c) 2025 Nicolas PICON
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions
 * of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

create table articles
(
    id             bigint auto_increment
        primary key,
    description    text          not null,
    link           varchar(2000) not null,
    title          varchar(255)  not null,
    publication_id bigint        not null
);

create index idx_publication_id
    on articles (publication_id);

create table publications
(
    id              bigint auto_increment
        primary key,
    date            datetime(6)  not null,
    newsletter_code varchar(255) not null,
    title           varchar(255) not null
);

alter table articles
    add constraint fk_article_publication
        foreign key (publication_id) references publications (id);

create index idx_newsletter_code
    on publications (newsletter_code);

create table github_issues
(
    issue_id       int          not null,
    issue_type varchar(31) not null,
    error_message  varchar(255),
    email_title    varchar(255),
    newsletter_url varchar(255),
    constraint pk_github_issues primary key (issue_id)
);

create index idx_github_issue_type
    on github_issues (issue_type);

create unique index idx_github_issue_newsletter_url
    on github_issues (newsletter_url);

create index idx_github_issue_email_processing
    on github_issues (email_title, error_message);
