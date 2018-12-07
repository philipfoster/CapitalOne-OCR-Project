
create extension pg_trgm;

create type image_type as enum ('JPEG', 'PDF', 'PNG', 'TIFF');

alter type image_type owner to ocr;

create type job_type as enum ('image', 'fingerprint');

alter type job_type owner to ocr;

create table addresses
(
    first_line  text,
    second_line text,
    city        text,
    state       text,
    postal_code text,
    country     text,
    id          serial not null
        constraint addresses_pkey
            primary key
);

alter table addresses
    owner to ocr;

create table document
(
    id                    serial                          not null
        constraint document_pkey
            primary key,
    account_number        bigint,
    ssn                   text,
    letter_date           date,
    postmark_date         date,
    date_of_birth         date,
    num_similar_documents integer default 0               not null,
    address               integer
        constraint document_addresses_id_fk
            references addresses,
    queue                 text    default 'general'::text not null,
    left_fingerprint      bigint  default 0,
    right_fingerprint     bigint  default 0
);

comment on table document is 'This table contains data extracted from the letter.';

comment on column document.letter_date is 'The date the letter was written';

comment on column document.num_similar_documents is 'The number of documents in the database that are substantially similar to this one at the time of ingest.';

alter table document
    owner to ocr;

create table document_images
(
    id           serial                not null
        constraint document_images_pkey
            primary key,
    file_data    bytea                 not null,
    page_number  integer,
    is_envelope  boolean default false not null,
    image_format image_type            not null,
    document_id  integer
        constraint document_images_document_id_fk
            references document
);

comment on table document_images is 'This table stores the original, unmodified scanned image';

comment on column document_images.page_number is 'Will be null iff is_envelope = true, otherwise this field will always have a value';

alter table document_images
    owner to ocr;

create table document_text
(
    id              serial   not null
        constraint document_text_pkey
            primary key,
    original_text   text     not null,
    vectorized_text tsvector not null,
    image_id        integer  not null
        constraint document_text_document_images_id_fk
            references document_images
);

comment on table document_text is 'Text for a single page of a document.';

alter table document_text
    owner to ocr;

create index document_text_vectorized_text_idx
    on document_text using gin(vectorized_text);


create table jobs
(
    created_at     timestamp default now() not null,
    id             serial                  not null
        constraint jobs_pkey
            primary key,
    document_image integer
        constraint jobs_document_images_id_fk
            references document_images,
    type           job_type,
    document_id    integer
        constraint jobs_document_id_fk
            references document
);

alter table jobs
    owner to ocr;

create table job_assignments
(
    server_id    uuid                    not null,
    accepted_at  timestamp default now() not null,
    completed_at timestamp,
    job_id       serial                  not null
        constraint job_assignments_jobs_id_fk
            references jobs
);

alter table job_assignments
    owner to ocr;

create index job_assignments_server_id_index
    on job_assignments (job_id);

create table job_dependency
(
    job        integer not null
        constraint job_dependency_jobs_id_fk
            references jobs,
    dependency integer not null
        constraint job_dependency_dependency_id_fk
            references jobs
);

alter table job_dependency
    owner to ocr;

create function set_document_tsvector_trigger() returns trigger
    language plpgsql
as
$$
BEGIN
    NEW.vectorized_text := to_tsvector(NEW.original_text);
    return NEW;
end;
$$;

alter function set_document_tsvector_trigger() owner to ocr;

CREATE TRIGGER trigger_set_tsvector BEFORE INSERT OR UPDATE ON public.document_text FOR EACH ROW EXECUTE PROCEDURE public.set_document_tsvector_trigger();

create function ngram(string text, n integer, OUT ngrams text[]) returns text[]
    language plpgsql
as
$$
declare
    tokens text[] := regexp_split_to_array(string, ' ');
    tmp    text   := '';
begin
    for i in 1..array_length(tokens, 1) - n + 1
        loop
            tmp := '';
            for j in 1..n
                loop
                    tmp := concat(tmp, ' ', tokens [ i + j - 1]);
                end loop;
                ngrams [ i] := trim(tmp);
        end loop;

end;
$$;

alter function ngram(text, integer, out text[]) owner to ocr;
