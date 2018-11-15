-- Update version 1 database to version 2

begin transaction;

truncate table addresses, document, document_text, document_images, document_queue_relation, job_assignments, jobs, queues;

-- Add hooks into custom db extension\
DROP FUNCTION IF EXISTS bytea_xor(bytea, bytea);
CREATE FUNCTION bytea_xor(bytea, bytea) RETURNS bytea
AS 'bytea_bitops.so', 'bytea_xor'
LANGUAGE C STRICT;

DROP FUNCTION IF EXISTS bytea_and(bytea, bytea);
CREATE FUNCTION bytea_and(bytea, bytea) RETURNS bytea
AS 'bytea_bitops.so', 'bytea_and'
LANGUAGE C STRICT;

DROP FUNCTION IF EXISTS bytea_or(bytea, bytea);
CREATE FUNCTION bytea_or(bytea, bytea) RETURNS bytea
AS 'bytea_bitops.so', 'bytea_or'
LANGUAGE C STRICT;

DROP FUNCTION IF EXISTS bytea_not(bytea);
CREATE FUNCTION bytea_not(bytea) RETURNS bytea
AS 'bytea_bitops.so', 'bytea_not'
LANGUAGE C STRICT;

DROP FUNCTION IF EXISTS bytea_bitsset(bytea);
CREATE FUNCTION bytea_bitsset(bytea) RETURNS integer
AS 'bytea_bitops.so', 'bytea_bitsset'
LANGUAGE C STRICT;

-- Update schema to add different job types and move fingerprint from document_text to document table
alter table document_text drop column if exists fingerprint;
alter table document add column if not exists fingerprint bytea;

COMMENT ON TABLE public.document_text IS 'Text for a single page of a document.';

do $$ begin
  create type job_type as enum ('image', 'fingerprint');
exception
  when duplicate_object then null;
end $$;


alter table jobs add column if not exists type job_type;

ALTER TABLE public.jobs ADD if not exists document_id int NULL;

do $$ begin
    ALTER TABLE public.jobs
      ADD CONSTRAINT jobs_document_id_fk
    FOREIGN KEY (document_id) REFERENCES public.document (id);
  exception
    when duplicate_object then null;
end $$;


ALTER TABLE public.jobs ALTER COLUMN document_image DROP NOT NULL;

-- Add table to represent job dependencies
CREATE TABLE IF NOT EXISTS public.job_dependency
(
  job int NOT NULL,
  dependency int NOT NULL,
  CONSTRAINT job_dependency_jobs_id_fk FOREIGN KEY (job) REFERENCES public.jobs (id),
  CONSTRAINT job_dependency_dependency_id_fk FOREIGN KEY (dependency) REFERENCES public.jobs (id)
);

end transaction;
