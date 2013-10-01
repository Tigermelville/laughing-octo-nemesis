create temporary table pgdump_restore_path(p text);
insert into pgdump_restore_path values('/tmp');
SET statement_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET search_path = public, pg_catalog;
CREATE OR REPLACE FUNCTION pseudo_encrypt(VALUE bigint) returns bigint AS $$
DECLARE
l1 int;
l2 int;
r1 int;
r2 int;
i int:=0;
BEGIN
 l1:= (VALUE >> 16) & 65535;
 r1:= VALUE & 65535;
 WHILE i < 3 LOOP
   l2 := r1;
   r2 := l1 # ((((1366.0 * r1 + 150889) % 714025) / 714025.0) * 32767)::int;
   l1 := l2;
   r1 := r2;
   i := i + 1;
 END LOOP;
 RETURN ((l1::bigint << 16) + r1);
END;
$$ LANGUAGE plpgsql strict immutable;

-- GENERATE-INDEX-DROPS
-- i.e. DROP INDEX public.index_users_on_email;

-- GENERATE-KEY-CONSTRAINT-DROPS
-- i.e. ALTER TABLE ONLY public.users DROP CONSTRAINT users_pkey;

DROP TABLE IF EXISTS t_short_url CASCADE;
-- DROP EXTENSION IF EXISTS plpgsql;
-- DROP SCHEMA IF EXISTS public;

-- CREATE SCHEMA public;
-- ALTER SCHEMA public OWNER TO postgres;
-- COMMENT ON SCHEMA public IS 'standard public schema';
-- CREATE EXTENSION IF NOT EXIST plpgsql WITH SCHEMA pg_catalog;
-- COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';
set search_path = public, pg_catalog;
set default_tablespace = '';
set default_with_oids = false;

CREATE TABLE t_short_url (
    f_id             bigint           NOT NULL,
    f_url            character varying(4095) NOT NULL,
    UNIQUE(f_url)
);

ALTER TABLE public.t_short_url OWNER TO shorty;
CREATE SEQUENCE public.short_url_id_seq
  START WITH 302
  INCREMENT BY 1
  NO MINVALUE
  NO MAXVALUE
  CACHE 1;
ALTER TABLE public.short_url_id_seq OWNER TO shorty;
ALTER SEQUENCE short_url_id_seq OWNED BY t_short_url.f_id;
ALTER TABLE ONLY t_short_url ALTER COLUMN f_id SET DEFAULT pseudo_encrypt(nextval('short_url_id_seq'::regclass)::bigint);

