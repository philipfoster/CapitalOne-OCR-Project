--
-- PostgreSQL database dump
--

-- Dumped from database version 10.5 (Debian 10.5-1.pgdg90+1)
-- Dumped by pg_dump version 10.5

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: postgres; Type: DATABASE; Schema: -; Owner: ocr
--

CREATE DATABASE postgres WITH TEMPLATE = template0 ENCODING = 'UTF8' LC_COLLATE = 'en_US.utf8' LC_CTYPE = 'en_US.utf8';


ALTER DATABASE postgres OWNER TO ocr;

\connect postgres

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: DATABASE postgres; Type: COMMENT; Schema: -; Owner: ocr
--

COMMENT ON DATABASE postgres IS 'default administrative connection database';


--
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


--
-- Name: pg_trgm; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS pg_trgm WITH SCHEMA public;


--
-- Name: EXTENSION pg_trgm; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION pg_trgm IS 'text similarity measurement and index searching based on trigrams';


--
-- Name: compare_simhash(bit, bit); Type: FUNCTION; Schema: public; Owner: ocr
--

CREATE FUNCTION public.compare_simhash(hash1 bit, hash2 bit, OUT similarity double precision) RETURNS double precision
    LANGUAGE plpgsql
    AS $$
declare
      bit_count integer := 0;
      hash_xor bit(128) := hash1 # hash2;
    begin
      for i in 1..128 loop
        if get_bit(hash_xor, i) = 1 then
          bit_count = bit_count + 1;
        end if;
      end loop;

      similarity := 1 - (bit_count/128);
    end;
$$;


ALTER FUNCTION public.compare_simhash(hash1 bit, hash2 bit, OUT similarity double precision) OWNER TO ocr;

--
-- Name: ngram(text, integer); Type: FUNCTION; Schema: public; Owner: ocr
--

CREATE FUNCTION public.ngram(string text, n integer, OUT ngrams text[]) RETURNS text[]
    LANGUAGE plpgsql
    AS $$
declare
    tokens text[] := regexp_split_to_array(string, ' ');
    tmp text := '';
  begin
    for i in 1..array_length(tokens, 1)-n+1 loop
      tmp := '';
      for j in 1..n loop
        tmp := concat(tmp, ' ', tokens[i+j-1]);
      end loop;
      ngrams[i] := trim(tmp);
    end loop;

  end;
$$;


ALTER FUNCTION public.ngram(string text, n integer, OUT ngrams text[]) OWNER TO ocr;

--
-- Name: set_document_tsvector_trigger(); Type: FUNCTION; Schema: public; Owner: ocr
--

CREATE FUNCTION public.set_document_tsvector_trigger() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    NEW.vectorized_text := to_tsvector(NEW.original_text);
    return NEW;
  end;
$$;


ALTER FUNCTION public.set_document_tsvector_trigger() OWNER TO ocr;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: addresses; Type: TABLE; Schema: public; Owner: ocr
--

CREATE TABLE public.addresses (
    first_line text,
    second_line text,
    city text,
    state text,
    postal_code text,
    country text,
    id integer NOT NULL
);


ALTER TABLE public.addresses OWNER TO ocr;

--
-- Name: addresses_id_seq; Type: SEQUENCE; Schema: public; Owner: ocr
--

CREATE SEQUENCE public.addresses_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.addresses_id_seq OWNER TO ocr;

--
-- Name: addresses_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: ocr
--

ALTER SEQUENCE public.addresses_id_seq OWNED BY public.addresses.id;


--
-- Name: document; Type: TABLE; Schema: public; Owner: ocr
--

CREATE TABLE public.document (
    id integer NOT NULL,
    account_number bigint,
    ssn text,
    letter_date date,
    postmark_date date,
    date_of_birth date,
    num_similar_documents integer DEFAULT 0 NOT NULL,
    address integer,
    text_id integer NOT NULL
);


ALTER TABLE public.document OWNER TO ocr;

--
-- Name: TABLE document; Type: COMMENT; Schema: public; Owner: ocr
--

COMMENT ON TABLE public.document IS 'This table contains data extracted from the letter.';


--
-- Name: COLUMN document.letter_date; Type: COMMENT; Schema: public; Owner: ocr
--

COMMENT ON COLUMN public.document.letter_date IS 'The date the letter was written';


--
-- Name: COLUMN document.num_similar_documents; Type: COMMENT; Schema: public; Owner: ocr
--

COMMENT ON COLUMN public.document.num_similar_documents IS 'The number of documents in the database that are substantially similar to this one at the time of ingest.';


--
-- Name: document_id_seq; Type: SEQUENCE; Schema: public; Owner: ocr
--

CREATE SEQUENCE public.document_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.document_id_seq OWNER TO ocr;

--
-- Name: document_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: ocr
--

ALTER SEQUENCE public.document_id_seq OWNED BY public.document.id;


--
-- Name: document_image_relation; Type: TABLE; Schema: public; Owner: ocr
--

CREATE TABLE public.document_image_relation (
    document_id integer NOT NULL,
    image_id integer NOT NULL
);


ALTER TABLE public.document_image_relation OWNER TO ocr;

--
-- Name: document_image_relation_document_id_seq; Type: SEQUENCE; Schema: public; Owner: ocr
--

CREATE SEQUENCE public.document_image_relation_document_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.document_image_relation_document_id_seq OWNER TO ocr;

--
-- Name: document_image_relation_document_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: ocr
--

ALTER SEQUENCE public.document_image_relation_document_id_seq OWNED BY public.document_image_relation.document_id;


--
-- Name: document_image_relation_image_id_seq; Type: SEQUENCE; Schema: public; Owner: ocr
--

CREATE SEQUENCE public.document_image_relation_image_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.document_image_relation_image_id_seq OWNER TO ocr;

--
-- Name: document_image_relation_image_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: ocr
--

ALTER SEQUENCE public.document_image_relation_image_id_seq OWNED BY public.document_image_relation.image_id;


--
-- Name: document_images; Type: TABLE; Schema: public; Owner: ocr
--

CREATE TABLE public.document_images (
    id integer NOT NULL,
    file_data bytea NOT NULL,
    file_name text NOT NULL,
    page_number integer,
    is_envelope boolean DEFAULT false NOT NULL
);


ALTER TABLE public.document_images OWNER TO ocr;

--
-- Name: TABLE document_images; Type: COMMENT; Schema: public; Owner: ocr
--

COMMENT ON TABLE public.document_images IS 'This table stores the original, unmodified scanned image';


--
-- Name: COLUMN document_images.page_number; Type: COMMENT; Schema: public; Owner: ocr
--

COMMENT ON COLUMN public.document_images.page_number IS 'Will be null iff is_envelope = true, otherwise this field will always have a value';


--
-- Name: document_images_id_seq; Type: SEQUENCE; Schema: public; Owner: ocr
--

CREATE SEQUENCE public.document_images_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.document_images_id_seq OWNER TO ocr;

--
-- Name: document_images_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: ocr
--

ALTER SEQUENCE public.document_images_id_seq OWNED BY public.document_images.id;


--
-- Name: document_queue_relation; Type: TABLE; Schema: public; Owner: ocr
--

CREATE TABLE public.document_queue_relation (
    document_id integer NOT NULL,
    queue_id integer NOT NULL
);


ALTER TABLE public.document_queue_relation OWNER TO ocr;

--
-- Name: document_queue_relation_document_id_seq; Type: SEQUENCE; Schema: public; Owner: ocr
--

CREATE SEQUENCE public.document_queue_relation_document_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.document_queue_relation_document_id_seq OWNER TO ocr;

--
-- Name: document_queue_relation_document_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: ocr
--

ALTER SEQUENCE public.document_queue_relation_document_id_seq OWNED BY public.document_queue_relation.document_id;


--
-- Name: document_queue_relation_queue_id_seq; Type: SEQUENCE; Schema: public; Owner: ocr
--

CREATE SEQUENCE public.document_queue_relation_queue_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.document_queue_relation_queue_id_seq OWNER TO ocr;

--
-- Name: document_queue_relation_queue_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: ocr
--

ALTER SEQUENCE public.document_queue_relation_queue_id_seq OWNED BY public.document_queue_relation.queue_id;


--
-- Name: document_text; Type: TABLE; Schema: public; Owner: ocr
--

CREATE TABLE public.document_text (
    id integer NOT NULL,
    original_text text NOT NULL,
    vectorized_text tsvector NOT NULL,
    fingerprint bytea
);


ALTER TABLE public.document_text OWNER TO ocr;

--
-- Name: COLUMN document_text.fingerprint; Type: COMMENT; Schema: public; Owner: ocr
--

COMMENT ON COLUMN public.document_text.fingerprint IS '128-bit simhash of the document text for duplicate detection';


--
-- Name: document_text_id_seq; Type: SEQUENCE; Schema: public; Owner: ocr
--

CREATE SEQUENCE public.document_text_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.document_text_id_seq OWNER TO ocr;

--
-- Name: document_text_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: ocr
--

ALTER SEQUENCE public.document_text_id_seq OWNED BY public.document_text.id;


--
-- Name: document_text_id_seq1; Type: SEQUENCE; Schema: public; Owner: ocr
--

CREATE SEQUENCE public.document_text_id_seq1
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.document_text_id_seq1 OWNER TO ocr;

--
-- Name: document_text_id_seq1; Type: SEQUENCE OWNED BY; Schema: public; Owner: ocr
--

ALTER SEQUENCE public.document_text_id_seq1 OWNED BY public.document.text_id;


--
-- Name: job_assignments; Type: TABLE; Schema: public; Owner: ocr
--

CREATE TABLE public.job_assignments (
    server_id uuid NOT NULL,
    accepted_at timestamp without time zone DEFAULT now() NOT NULL,
    completed_at timestamp without time zone,
    job_id integer NOT NULL
);


ALTER TABLE public.job_assignments OWNER TO ocr;

--
-- Name: job_assignments_job_id_seq; Type: SEQUENCE; Schema: public; Owner: ocr
--

CREATE SEQUENCE public.job_assignments_job_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.job_assignments_job_id_seq OWNER TO ocr;

--
-- Name: job_assignments_job_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: ocr
--

ALTER SEQUENCE public.job_assignments_job_id_seq OWNED BY public.job_assignments.job_id;


--
-- Name: jobs; Type: TABLE; Schema: public; Owner: ocr
--

CREATE TABLE public.jobs (
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    id integer NOT NULL,
    document_image integer NOT NULL
);


ALTER TABLE public.jobs OWNER TO ocr;

--
-- Name: jobs_document_image_seq; Type: SEQUENCE; Schema: public; Owner: ocr
--

CREATE SEQUENCE public.jobs_document_image_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.jobs_document_image_seq OWNER TO ocr;

--
-- Name: jobs_document_image_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: ocr
--

ALTER SEQUENCE public.jobs_document_image_seq OWNED BY public.jobs.document_image;


--
-- Name: jobs_id_seq; Type: SEQUENCE; Schema: public; Owner: ocr
--

CREATE SEQUENCE public.jobs_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.jobs_id_seq OWNER TO ocr;

--
-- Name: jobs_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: ocr
--

ALTER SEQUENCE public.jobs_id_seq OWNED BY public.jobs.id;


--
-- Name: queues; Type: TABLE; Schema: public; Owner: ocr
--

CREATE TABLE public.queues (
    queue_name text NOT NULL,
    id integer NOT NULL
);


ALTER TABLE public.queues OWNER TO ocr;

--
-- Name: queues_id_seq; Type: SEQUENCE; Schema: public; Owner: ocr
--

CREATE SEQUENCE public.queues_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.queues_id_seq OWNER TO ocr;

--
-- Name: queues_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: ocr
--

ALTER SEQUENCE public.queues_id_seq OWNED BY public.queues.id;


--
-- Name: addresses id; Type: DEFAULT; Schema: public; Owner: ocr
--

ALTER TABLE ONLY public.addresses ALTER COLUMN id SET DEFAULT nextval('public.addresses_id_seq'::regclass);


--
-- Name: document id; Type: DEFAULT; Schema: public; Owner: ocr
--

ALTER TABLE ONLY public.document ALTER COLUMN id SET DEFAULT nextval('public.document_id_seq'::regclass);


--
-- Name: document text_id; Type: DEFAULT; Schema: public; Owner: ocr
--

ALTER TABLE ONLY public.document ALTER COLUMN text_id SET DEFAULT nextval('public.document_text_id_seq1'::regclass);


--
-- Name: document_image_relation document_id; Type: DEFAULT; Schema: public; Owner: ocr
--

ALTER TABLE ONLY public.document_image_relation ALTER COLUMN document_id SET DEFAULT nextval('public.document_image_relation_document_id_seq'::regclass);


--
-- Name: document_image_relation image_id; Type: DEFAULT; Schema: public; Owner: ocr
--

ALTER TABLE ONLY public.document_image_relation ALTER COLUMN image_id SET DEFAULT nextval('public.document_image_relation_image_id_seq'::regclass);


--
-- Name: document_images id; Type: DEFAULT; Schema: public; Owner: ocr
--

ALTER TABLE ONLY public.document_images ALTER COLUMN id SET DEFAULT nextval('public.document_images_id_seq'::regclass);


--
-- Name: document_queue_relation document_id; Type: DEFAULT; Schema: public; Owner: ocr
--

ALTER TABLE ONLY public.document_queue_relation ALTER COLUMN document_id SET DEFAULT nextval('public.document_queue_relation_document_id_seq'::regclass);


--
-- Name: document_queue_relation queue_id; Type: DEFAULT; Schema: public; Owner: ocr
--

ALTER TABLE ONLY public.document_queue_relation ALTER COLUMN queue_id SET DEFAULT nextval('public.document_queue_relation_queue_id_seq'::regclass);


--
-- Name: document_text id; Type: DEFAULT; Schema: public; Owner: ocr
--

ALTER TABLE ONLY public.document_text ALTER COLUMN id SET DEFAULT nextval('public.document_text_id_seq'::regclass);


--
-- Name: job_assignments job_id; Type: DEFAULT; Schema: public; Owner: ocr
--

ALTER TABLE ONLY public.job_assignments ALTER COLUMN job_id SET DEFAULT nextval('public.job_assignments_job_id_seq'::regclass);


--
-- Name: jobs id; Type: DEFAULT; Schema: public; Owner: ocr
--

ALTER TABLE ONLY public.jobs ALTER COLUMN id SET DEFAULT nextval('public.jobs_id_seq'::regclass);


--
-- Name: jobs document_image; Type: DEFAULT; Schema: public; Owner: ocr
--

ALTER TABLE ONLY public.jobs ALTER COLUMN document_image SET DEFAULT nextval('public.jobs_document_image_seq'::regclass);


--
-- Name: queues id; Type: DEFAULT; Schema: public; Owner: ocr
--

ALTER TABLE ONLY public.queues ALTER COLUMN id SET DEFAULT nextval('public.queues_id_seq'::regclass);


--
-- Data for Name: addresses; Type: TABLE DATA; Schema: public; Owner: ocr
--



--
-- Data for Name: document; Type: TABLE DATA; Schema: public; Owner: ocr
--



--
-- Data for Name: document_image_relation; Type: TABLE DATA; Schema: public; Owner: ocr
--



--
-- Data for Name: document_images; Type: TABLE DATA; Schema: public; Owner: ocr
--



--
-- Data for Name: document_queue_relation; Type: TABLE DATA; Schema: public; Owner: ocr
--



--
-- Data for Name: document_text; Type: TABLE DATA; Schema: public; Owner: ocr
--



--
-- Data for Name: job_assignments; Type: TABLE DATA; Schema: public; Owner: ocr
--



--
-- Data for Name: jobs; Type: TABLE DATA; Schema: public; Owner: ocr
--



--
-- Data for Name: queues; Type: TABLE DATA; Schema: public; Owner: ocr
--



--
-- Name: addresses_id_seq; Type: SEQUENCE SET; Schema: public; Owner: ocr
--

SELECT pg_catalog.setval('public.addresses_id_seq', 1, false);


--
-- Name: document_id_seq; Type: SEQUENCE SET; Schema: public; Owner: ocr
--

SELECT pg_catalog.setval('public.document_id_seq', 1, false);


--
-- Name: document_image_relation_document_id_seq; Type: SEQUENCE SET; Schema: public; Owner: ocr
--

SELECT pg_catalog.setval('public.document_image_relation_document_id_seq', 1, false);


--
-- Name: document_image_relation_image_id_seq; Type: SEQUENCE SET; Schema: public; Owner: ocr
--

SELECT pg_catalog.setval('public.document_image_relation_image_id_seq', 1, false);


--
-- Name: document_images_id_seq; Type: SEQUENCE SET; Schema: public; Owner: ocr
--

SELECT pg_catalog.setval('public.document_images_id_seq', 1, false);


--
-- Name: document_queue_relation_document_id_seq; Type: SEQUENCE SET; Schema: public; Owner: ocr
--

SELECT pg_catalog.setval('public.document_queue_relation_document_id_seq', 1, false);


--
-- Name: document_queue_relation_queue_id_seq; Type: SEQUENCE SET; Schema: public; Owner: ocr
--

SELECT pg_catalog.setval('public.document_queue_relation_queue_id_seq', 1, false);


--
-- Name: document_text_id_seq; Type: SEQUENCE SET; Schema: public; Owner: ocr
--

SELECT pg_catalog.setval('public.document_text_id_seq', 1, false);


--
-- Name: document_text_id_seq1; Type: SEQUENCE SET; Schema: public; Owner: ocr
--

SELECT pg_catalog.setval('public.document_text_id_seq1', 1, false);


--
-- Name: job_assignments_job_id_seq; Type: SEQUENCE SET; Schema: public; Owner: ocr
--

SELECT pg_catalog.setval('public.job_assignments_job_id_seq', 1, false);


--
-- Name: jobs_document_image_seq; Type: SEQUENCE SET; Schema: public; Owner: ocr
--

SELECT pg_catalog.setval('public.jobs_document_image_seq', 1, false);


--
-- Name: jobs_id_seq; Type: SEQUENCE SET; Schema: public; Owner: ocr
--

SELECT pg_catalog.setval('public.jobs_id_seq', 1, false);


--
-- Name: queues_id_seq; Type: SEQUENCE SET; Schema: public; Owner: ocr
--

SELECT pg_catalog.setval('public.queues_id_seq', 1, false);


--
-- Name: addresses addresses_pkey; Type: CONSTRAINT; Schema: public; Owner: ocr
--

ALTER TABLE ONLY public.addresses
    ADD CONSTRAINT addresses_pkey PRIMARY KEY (id);


--
-- Name: document_image_relation document_image_relation_pk; Type: CONSTRAINT; Schema: public; Owner: ocr
--

ALTER TABLE ONLY public.document_image_relation
    ADD CONSTRAINT document_image_relation_pk PRIMARY KEY (document_id, image_id);


--
-- Name: document_images document_images_pkey; Type: CONSTRAINT; Schema: public; Owner: ocr
--

ALTER TABLE ONLY public.document_images
    ADD CONSTRAINT document_images_pkey PRIMARY KEY (id);


--
-- Name: document document_pkey; Type: CONSTRAINT; Schema: public; Owner: ocr
--

ALTER TABLE ONLY public.document
    ADD CONSTRAINT document_pkey PRIMARY KEY (id);


--
-- Name: document_queue_relation document_queue_relation_pk; Type: CONSTRAINT; Schema: public; Owner: ocr
--

ALTER TABLE ONLY public.document_queue_relation
    ADD CONSTRAINT document_queue_relation_pk PRIMARY KEY (document_id, queue_id);


--
-- Name: queues document_queues_pkey; Type: CONSTRAINT; Schema: public; Owner: ocr
--

ALTER TABLE ONLY public.queues
    ADD CONSTRAINT document_queues_pkey PRIMARY KEY (id);


--
-- Name: document_text document_text_pkey; Type: CONSTRAINT; Schema: public; Owner: ocr
--

ALTER TABLE ONLY public.document_text
    ADD CONSTRAINT document_text_pkey PRIMARY KEY (id);


--
-- Name: jobs jobs_pkey; Type: CONSTRAINT; Schema: public; Owner: ocr
--

ALTER TABLE ONLY public.jobs
    ADD CONSTRAINT jobs_pkey PRIMARY KEY (id);


--
-- Name: document_queues_queue_name_uindex; Type: INDEX; Schema: public; Owner: ocr
--

CREATE UNIQUE INDEX document_queues_queue_name_uindex ON public.queues USING btree (queue_name);


--
-- Name: document_text_vectorized_text_idx; Type: INDEX; Schema: public; Owner: ocr
--

CREATE INDEX document_text_vectorized_text_idx ON public.document_text USING btree (vectorized_text);


--
-- Name: document_text_vectorized_text_idx1; Type: INDEX; Schema: public; Owner: ocr
--

CREATE INDEX document_text_vectorized_text_idx1 ON public.document_text USING btree (vectorized_text);


--
-- Name: document_text trigger_set_tsvector; Type: TRIGGER; Schema: public; Owner: ocr
--

CREATE TRIGGER trigger_set_tsvector BEFORE INSERT OR UPDATE ON public.document_text FOR EACH ROW EXECUTE PROCEDURE public.set_document_tsvector_trigger();


--
-- Name: document document_addresses_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: ocr
--

ALTER TABLE ONLY public.document
    ADD CONSTRAINT document_addresses_id_fk FOREIGN KEY (address) REFERENCES public.addresses(id);


--
-- Name: document document_document_text_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: ocr
--

ALTER TABLE ONLY public.document
    ADD CONSTRAINT document_document_text_id_fk FOREIGN KEY (text_id) REFERENCES public.document_text(id);


--
-- Name: document_image_relation document_image_relation_document_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: ocr
--

ALTER TABLE ONLY public.document_image_relation
    ADD CONSTRAINT document_image_relation_document_id_fk FOREIGN KEY (document_id) REFERENCES public.document(id);


--
-- Name: document_image_relation document_image_relation_document_images_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: ocr
--

ALTER TABLE ONLY public.document_image_relation
    ADD CONSTRAINT document_image_relation_document_images_id_fk FOREIGN KEY (image_id) REFERENCES public.document_images(id);


--
-- Name: document_queue_relation document_queue_relation_document_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: ocr
--

ALTER TABLE ONLY public.document_queue_relation
    ADD CONSTRAINT document_queue_relation_document_id_fk FOREIGN KEY (document_id) REFERENCES public.document(id);


--
-- Name: document_queue_relation document_queue_relation_queues_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: ocr
--

ALTER TABLE ONLY public.document_queue_relation
    ADD CONSTRAINT document_queue_relation_queues_id_fk FOREIGN KEY (queue_id) REFERENCES public.queues(id);


--
-- Name: job_assignments job_assignments_jobs_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: ocr
--

ALTER TABLE ONLY public.job_assignments
    ADD CONSTRAINT job_assignments_jobs_id_fk FOREIGN KEY (job_id) REFERENCES public.jobs(id);


--
-- Name: jobs jobs_document_images_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: ocr
--

ALTER TABLE ONLY public.jobs
    ADD CONSTRAINT jobs_document_images_id_fk FOREIGN KEY (document_image) REFERENCES public.document_images(id);


--
-- PostgreSQL database dump complete
--

