--
-- PostgreSQL database dump
--

-- Dumped from database version 16.4
-- Dumped by pg_dump version 16.4

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: pgcrypto; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS pgcrypto WITH SCHEMA public;


--
-- Name: EXTENSION pgcrypto; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION pgcrypto IS 'cryptographic functions';


--
-- Name: unaccent; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS unaccent WITH SCHEMA public;


--
-- Name: EXTENSION unaccent; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION unaccent IS 'text search dictionary that removes accents';


--
-- Name: check_telephone_sites(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.check_telephone_sites() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
  -- Vérifie si le meme téléphone existe déjà sur un autre site
  IF EXISTS (
       SELECT 1 FROM Patrouilleurs
        WHERE telephone = NEW.telephone
          AND Id_Site <> NEW.Id_Site
     ) THEN
     RAISE EXCEPTION
       'Le téléphone % est déjà utilisé sur un autre site', NEW.telephone;
  END IF;

  RETURN NEW;
END;
$$;


ALTER FUNCTION public.check_telephone_sites() OWNER TO postgres;

--
-- Name: sync_responsable_to_user(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.sync_responsable_to_user() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
    fonction_id INT;
    email_pattern VARCHAR;
BEGIN
    -- Vérifier si c'est un responsable
    IF NEW.role = 'Responsable' THEN
        -- Récupérer l'ID de la fonction "Responsable"
        SELECT Id_Fonction INTO fonction_id 
        FROM Fonction 
        WHERE fonction = 'Responsable';
        
        -- Utiliser l'email du patrouilleur s'il existe, sinon générer un email
        IF NEW.email IS NOT NULL AND NEW.email != '' THEN
            email_pattern := NEW.email;
        ELSE
            email_pattern := LOWER(REPLACE(UNACCENT(NEW.Nom), ' ', '.')) || '@patrouille.mg';
        END IF;
        
        -- Insérer dans User_ avec mot de passe simple (à changer par l'utilisateur)
        INSERT INTO User_ (Nom, email, Telephone, Adresse, mot_de_passe, Id_Fonction)
        VALUES (
            NEW.Nom,
            email_pattern,
            NEW.telephone,
            'Antananarivo',
            -- Mot de passe par défaut simple (l'utilisateur devra le changer)
            'responsable123',
            fonction_id
        )
        ON CONFLICT (email) DO NOTHING;
        
    END IF;
    
    RETURN NEW;
END;
$$;


ALTER FUNCTION public.sync_responsable_to_user() OWNER TO postgres;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: alerte; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.alerte (
    id_alerte integer NOT NULL,
    id_site integer NOT NULL,
    id_message integer NOT NULL,
    id_typealerte integer NOT NULL
);


ALTER TABLE public.alerte OWNER TO postgres;

--
-- Name: alerte_id_alerte_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.alerte_id_alerte_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.alerte_id_alerte_seq OWNER TO postgres;

--
-- Name: alerte_id_alerte_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.alerte_id_alerte_seq OWNED BY public.alerte.id_alerte;


--
-- Name: fonction; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.fonction (
    id_fonction integer NOT NULL,
    fonction character varying(255)
);


ALTER TABLE public.fonction OWNER TO postgres;

--
-- Name: fonction_id_fonction_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.fonction_id_fonction_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.fonction_id_fonction_seq OWNER TO postgres;

--
-- Name: fonction_id_fonction_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.fonction_id_fonction_seq OWNED BY public.fonction.id_fonction;


--
-- Name: fonction_zonealerte; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.fonction_zonealerte (
    id_fonction integer NOT NULL,
    id_typealerte integer NOT NULL
);


ALTER TABLE public.fonction_zonealerte OWNER TO postgres;

--
-- Name: historique_message_status; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.historique_message_status (
    id_historique integer NOT NULL,
    date_changement timestamp without time zone,
    id_status integer,
    id_message integer
);


ALTER TABLE public.historique_message_status OWNER TO postgres;

--
-- Name: historique_message_status_id_historique_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.historique_message_status_id_historique_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.historique_message_status_id_historique_seq OWNER TO postgres;

--
-- Name: historique_message_status_id_historique_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.historique_message_status_id_historique_seq OWNED BY public.historique_message_status.id_historique;


--
-- Name: historique_status_agent; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.historique_status_agent (
    id_historique integer NOT NULL,
    date_changement timestamp without time zone,
    id_status_agent integer NOT NULL,
    id_patrouilleur integer NOT NULL
);


ALTER TABLE public.historique_status_agent OWNER TO postgres;

--
-- Name: historique_status_agent_id_historique_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.historique_status_agent_id_historique_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.historique_status_agent_id_historique_seq OWNER TO postgres;

--
-- Name: historique_status_agent_id_historique_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.historique_status_agent_id_historique_seq OWNED BY public.historique_status_agent.id_historique;


--
-- Name: intervention; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.intervention (
    id_intervention integer NOT NULL,
    intervention character varying(255)
);


ALTER TABLE public.intervention OWNER TO postgres;

--
-- Name: intervention_id_intervention_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.intervention_id_intervention_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.intervention_id_intervention_seq OWNER TO postgres;

--
-- Name: intervention_id_intervention_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.intervention_id_intervention_seq OWNED BY public.intervention.id_intervention;


--
-- Name: message; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.message (
    id_message integer NOT NULL,
    date_commencement timestamp without time zone NOT NULL,
    date_signalement timestamp without time zone NOT NULL,
    pointrepere character varying(255),
    surface_approximative double precision,
    description character varying(255),
    direction character varying(20) NOT NULL,
    renfort boolean,
    longitude double precision,
    latitude double precision,
    id_intervention integer NOT NULL,
    iduserapp integer NOT NULL,
    CONSTRAINT ck_dates CHECK ((date_signalement > date_commencement))
);


ALTER TABLE public.message OWNER TO postgres;

--
-- Name: message_id_message_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.message_id_message_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.message_id_message_seq OWNER TO postgres;

--
-- Name: message_id_message_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.message_id_message_seq OWNED BY public.message.id_message;


--
-- Name: message_patrouilleur; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.message_patrouilleur (
    id_message_patrouilleur integer NOT NULL,
    id_patrouilleur integer NOT NULL,
    id_message integer NOT NULL
);


ALTER TABLE public.message_patrouilleur OWNER TO postgres;

--
-- Name: message_patrouilleur_id_message_patrouilleur_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.message_patrouilleur_id_message_patrouilleur_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.message_patrouilleur_id_message_patrouilleur_seq OWNER TO postgres;

--
-- Name: message_patrouilleur_id_message_patrouilleur_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.message_patrouilleur_id_message_patrouilleur_seq OWNED BY public.message_patrouilleur.id_message_patrouilleur;


--
-- Name: patrouilleurs; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.patrouilleurs (
    id_patrouilleur integer NOT NULL,
    nom character varying(100) NOT NULL,
    role character varying(50),
    telephone character varying(20) NOT NULL,
    date_recrutement timestamp(6) without time zone,
    id_site integer NOT NULL,
    email character varying(255)
);


ALTER TABLE public.patrouilleurs OWNER TO postgres;

--
-- Name: patrouilleurs_id_patrouilleur_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.patrouilleurs_id_patrouilleur_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.patrouilleurs_id_patrouilleur_seq OWNER TO postgres;

--
-- Name: patrouilleurs_id_patrouilleur_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.patrouilleurs_id_patrouilleur_seq OWNED BY public.patrouilleurs.id_patrouilleur;


--
-- Name: site; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.site (
    id_site integer NOT NULL,
    nom character varying(255) NOT NULL,
    region character varying(255) NOT NULL,
    surface double precision,
    decree character varying(255),
    latitude double precision,
    longitude double precision
);


ALTER TABLE public.site OWNER TO postgres;

--
-- Name: site_id_site_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.site_id_site_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.site_id_site_seq OWNER TO postgres;

--
-- Name: site_id_site_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.site_id_site_seq OWNED BY public.site.id_site;


--
-- Name: status_agent; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.status_agent (
    id_status_agent integer NOT NULL,
    status character varying(50) NOT NULL
);


ALTER TABLE public.status_agent OWNER TO postgres;

--
-- Name: status_agent_id_status_agent_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.status_agent_id_status_agent_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.status_agent_id_status_agent_seq OWNER TO postgres;

--
-- Name: status_agent_id_status_agent_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.status_agent_id_status_agent_seq OWNED BY public.status_agent.id_status_agent;


--
-- Name: status_message; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.status_message (
    id_status_message integer NOT NULL,
    status character varying(255) NOT NULL
);


ALTER TABLE public.status_message OWNER TO postgres;

--
-- Name: status_message_id_status_message_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.status_message_id_status_message_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.status_message_id_status_message_seq OWNER TO postgres;

--
-- Name: status_message_id_status_message_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.status_message_id_status_message_seq OWNED BY public.status_message.id_status_message;


--
-- Name: typealerte; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.typealerte (
    id_typealerte integer NOT NULL,
    zone character varying(255) NOT NULL
);


ALTER TABLE public.typealerte OWNER TO postgres;

--
-- Name: typealerte_id_typealerte_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.typealerte_id_typealerte_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.typealerte_id_typealerte_seq OWNER TO postgres;

--
-- Name: typealerte_id_typealerte_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.typealerte_id_typealerte_seq OWNED BY public.typealerte.id_typealerte;


--
-- Name: user_; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.user_ (
    id_user integer NOT NULL,
    nom character varying(50) NOT NULL,
    email character varying(50) NOT NULL,
    telephone character varying(50) NOT NULL,
    adresse character varying(50) NOT NULL,
    mot_de_passe character varying(155),
    id_fonction integer NOT NULL
);


ALTER TABLE public.user_ OWNER TO postgres;

--
-- Name: user__id_user_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.user__id_user_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.user__id_user_seq OWNER TO postgres;

--
-- Name: user__id_user_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.user__id_user_seq OWNED BY public.user_.id_user;


--
-- Name: userapp; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.userapp (
    iduserapp integer NOT NULL,
    motdepasse character varying(255) NOT NULL,
    login character varying(255) NOT NULL,
    id_patrouilleur integer NOT NULL
);


ALTER TABLE public.userapp OWNER TO postgres;

--
-- Name: userapp_iduserapp_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.userapp_iduserapp_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.userapp_iduserapp_seq OWNER TO postgres;

--
-- Name: userapp_iduserapp_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.userapp_iduserapp_seq OWNED BY public.userapp.iduserapp;


--
-- Name: alerte id_alerte; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.alerte ALTER COLUMN id_alerte SET DEFAULT nextval('public.alerte_id_alerte_seq'::regclass);


--
-- Name: fonction id_fonction; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fonction ALTER COLUMN id_fonction SET DEFAULT nextval('public.fonction_id_fonction_seq'::regclass);


--
-- Name: historique_message_status id_historique; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.historique_message_status ALTER COLUMN id_historique SET DEFAULT nextval('public.historique_message_status_id_historique_seq'::regclass);


--
-- Name: historique_status_agent id_historique; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.historique_status_agent ALTER COLUMN id_historique SET DEFAULT nextval('public.historique_status_agent_id_historique_seq'::regclass);


--
-- Name: intervention id_intervention; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.intervention ALTER COLUMN id_intervention SET DEFAULT nextval('public.intervention_id_intervention_seq'::regclass);


--
-- Name: message id_message; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.message ALTER COLUMN id_message SET DEFAULT nextval('public.message_id_message_seq'::regclass);


--
-- Name: message_patrouilleur id_message_patrouilleur; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.message_patrouilleur ALTER COLUMN id_message_patrouilleur SET DEFAULT nextval('public.message_patrouilleur_id_message_patrouilleur_seq'::regclass);


--
-- Name: patrouilleurs id_patrouilleur; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.patrouilleurs ALTER COLUMN id_patrouilleur SET DEFAULT nextval('public.patrouilleurs_id_patrouilleur_seq'::regclass);


--
-- Name: site id_site; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.site ALTER COLUMN id_site SET DEFAULT nextval('public.site_id_site_seq'::regclass);


--
-- Name: status_agent id_status_agent; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.status_agent ALTER COLUMN id_status_agent SET DEFAULT nextval('public.status_agent_id_status_agent_seq'::regclass);


--
-- Name: status_message id_status_message; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.status_message ALTER COLUMN id_status_message SET DEFAULT nextval('public.status_message_id_status_message_seq'::regclass);


--
-- Name: typealerte id_typealerte; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.typealerte ALTER COLUMN id_typealerte SET DEFAULT nextval('public.typealerte_id_typealerte_seq'::regclass);


--
-- Name: user_ id_user; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_ ALTER COLUMN id_user SET DEFAULT nextval('public.user__id_user_seq'::regclass);


--
-- Name: userapp iduserapp; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.userapp ALTER COLUMN iduserapp SET DEFAULT nextval('public.userapp_iduserapp_seq'::regclass);


--
-- Data for Name: alerte; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.alerte (id_alerte, id_site, id_message, id_typealerte) FROM stdin;
1	3	2	4
2	3	6	3
3	3	7	2
5	3	10	1
6	4	11	4
7	2	12	4
8	2	13	4
9	2	14	4
10	2	15	4
11	2	16	4
12	2	17	4
13	2	18	4
14	2	19	4
15	2	20	4
16	2	21	4
17	2	22	4
18	2	23	2
19	2	24	2
20	2	25	2
21	2	26	2
22	2	36	4
23	2	47	4
24	2	51	4
25	2	55	4
26	2	58	4
\.


--
-- Data for Name: fonction; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.fonction (id_fonction, fonction) FROM stdin;
1	Directeur National
2	Administrateur
3	Subordonnée
4	Responsable
\.


--
-- Data for Name: fonction_zonealerte; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.fonction_zonealerte (id_fonction, id_typealerte) FROM stdin;
1	1
2	1
2	2
2	3
2	4
3	1
3	2
4	1
4	2
4	3
\.


--
-- Data for Name: historique_message_status; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.historique_message_status (id_historique, date_changement, id_status, id_message) FROM stdin;
1	2025-09-05 19:23:02.537821	1	2
2	2025-09-05 22:03:17.982591	1	6
3	2025-09-05 22:14:51.283895	2	7
6	2025-09-06 05:25:37.168109	2	10
7	2025-09-06 23:32:36.072079	1	11
8	2025-09-05 10:13:40	3	2
9	2025-09-10 17:14:14.843616	1	12
10	2025-09-10 20:22:27.304738	1	13
11	2025-09-10 20:29:39.614159	1	14
12	2025-09-10 21:26:24.210809	1	15
13	2025-09-10 21:47:44.871689	1	16
14	2025-09-10 22:09:08.203571	1	17
15	2025-09-11 01:29:41.215755	1	18
16	2025-09-11 01:37:48.390992	1	19
17	2025-09-11 01:50:54.130791	1	20
18	2025-09-11 02:19:15.824485	1	21
19	2025-09-11 02:23:27.99219	1	22
20	2025-09-11 11:00:47.862247	2	23
21	2025-09-11 11:13:20.283051	2	24
22	2025-09-11 11:40:59.867536	2	25
23	2025-09-11 11:50:29.596045	2	26
24	2025-09-11 16:28:35.865916	1	36
25	2025-12-23 12:17:53.254573	1	47
26	2025-12-23 12:27:52.822643	1	51
27	2025-12-23 13:12:15	3	26
29	2025-12-23 13:14:15.730988	1	55
30	2025-12-23 13:15:07	2	55
31	2026-01-03 11:09:35	2	51
32	2026-01-03 11:15:15.365252	1	58
\.


--
-- Data for Name: historique_status_agent; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.historique_status_agent (id_historique, date_changement, id_status_agent, id_patrouilleur) FROM stdin;
\.


--
-- Data for Name: intervention; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.intervention (id_intervention, intervention) FROM stdin;
1	Possible
2	Partielle
3	Impossible
\.


--
-- Data for Name: message; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.message (id_message, date_commencement, date_signalement, pointrepere, surface_approximative, description, direction, renfort, longitude, latitude, id_intervention, iduserapp) FROM stdin;
2	2025-09-05 10:00:00	2025-09-05 10:05:00	PointA	50	Petit feu	Sud	f	47.50789	-18.87919	1	6
6	2025-09-05 10:00:00	2025-09-05 10:05:00	PointB	150.5	Feu moyen	Sud	f	47.508	-18.88	2	6
7	2025-09-05 10:00:00	2025-09-05 10:05:00	PointC	300	Feu important	Sud	t	47.509	-18.881	2	5
10	2025-09-05 10:00:00	2025-09-05 10:05:00	PointD	500	Feu critique	Sud	f	47.51	-18.882	3	4
11	2025-09-05 10:00:00	2025-09-05 10:05:00	PointA	50	Petit feu	Sud	f	47.10789	-18.87918	1	4
12	2025-09-05 10:00:00	2025-09-05 10:05:00	PointE	900	Feu faible	Sud	f	47.511	-18.883	1	6
13	2025-09-05 10:00:00	2025-09-05 10:05:00	PointE	900	Feu faible	Sud	f	47.51188	-18.8839	1	6
14	2025-09-05 10:00:00	2025-09-05 10:05:00	PointE	900	Feu faible	Sud	f	47.51186	-18.88397	1	6
15	2025-09-05 10:00:00	2025-09-05 10:05:00	PointE	900	Feu faible	Sud	f	47.91186	-18.98397	1	6
16	2025-09-05 10:00:00	2025-09-05 10:05:00	PointE	900	Feu faible	Sud	f	46.91186	-17.98397	1	7
17	2025-09-05 10:00:00	2025-09-05 10:05:00	PointE	900	Feu faible	Sud	f	46.01186	-17.08397	1	6
18	2025-09-05 10:00:00	2025-09-05 10:05:00	PointE	900	Feu faible	Sud	f	46.01886	-17.08399	1	6
19	2025-09-05 10:00:00	2025-09-05 10:05:00	PointE	900	Feu faible	Sud	f	46.11886	-17.18399	1	6
20	2025-09-05 10:00:00	2025-09-05 10:05:00	PointE	900	Feu faible	Sud	f	46.11896	-17.18389	1	6
21	2025-09-05 10:00:00	2025-09-05 10:05:00	PointE	900	Feu faible	Sud	f	46.31896	-17.28389	1	6
22	2025-09-05 10:00:00	2025-09-05 10:05:00	PointE	900	Feu faible	Sud	f	46.32896	-17.21389	1	6
23	2025-09-05 10:00:00	2025-09-05 10:05:00	PointE	900	Feu important	Sud	t	47.32896	-18.21389	2	5
24	2025-09-05 10:00:00	2025-09-05 10:05:00	PointE	900	Feu important	Sud	t	47.22896	-18.23389	2	5
25	2025-09-05 10:00:00	2025-09-05 10:05:00	PointE	900	Feu important	Sud	t	47.24896	-18.23387	2	5
26	2025-09-05 10:00:00	2025-09-05 10:05:00	PointE	900	Feu important	Sud	t	47.14896	-18.27387	2	5
36	2025-09-11 15:29:00	2025-09-11 16:29:51.31	\N	2184	\N	Est	f	-18.9190408	47.5452566	1	7
47	2025-09-11 15:29:00	2025-09-11 16:29:51.31	\N	2184	\N	Est	f	47.5452566	-18.9190408	1	7
51	2025-12-01 03:27:00	2025-12-23 12:27:50.002	\N	0	\N	Est	f	47.5325984	-18.9859875	1	5
55	2025-12-01 13:13:00	2025-12-23 13:14:10.09	\N	23	\N	Est	f	47.5326001	-18.9859918	1	5
58	2025-12-24 11:15:00	2026-01-03 11:15:13.392	point	21	feu	Est	f	47.5453799	-18.9190058	1	5
\.


--
-- Data for Name: message_patrouilleur; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.message_patrouilleur (id_message_patrouilleur, id_patrouilleur, id_message) FROM stdin;
\.


--
-- Data for Name: patrouilleurs; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.patrouilleurs (id_patrouilleur, nom, role, telephone, date_recrutement, id_site, email) FROM stdin;
6	Rakotoarisoa Marie	Agent	+261383817421	2021-03-20 00:00:00	2	\N
7	Andrianasolo Hery	Agent	+261382305086	2019-11-10 00:00:00	3	\N
8	Andrianasolo Hery	Agent	+261384984929	2019-11-10 00:00:00	4	\N
5	Rasolofoniaina Jean	Responsable	+261349322431	2020-01-15 00:00:00	1	harimalalaerickarandria@gmail.com
10	RAZAFIMANANTSOA Tsiresy	Agent	+261346367580	2020-01-15 00:00:00	3	mntsiresy@gmail.com
\.


--
-- Data for Name: site; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.site (id_site, nom, region, surface, decree, latitude, longitude) FROM stdin;
1	Tsimembo-Manambolomaty	Menabe	50000	Décret 2002-123	-19.8	44.5
2	Mandrozo	Menabe	45000	Décret 2002-124	-19.7	44.6
3	Bemanevika	Sofia	35000	Décret 2002-125	-14.5	49.8
4	Mahimborondro	Sofia	30000	Décret 2002-126	-14.6	49.9
\.


--
-- Data for Name: status_agent; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.status_agent (id_status_agent, status) FROM stdin;
1	Disponible
2	En mission
3	Indisponible
\.


--
-- Data for Name: status_message; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.status_message (id_status_message, status) FROM stdin;
1	Debut de feu
2	En cours
3	Maitrise
\.


--
-- Data for Name: typealerte; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.typealerte (id_typealerte, zone) FROM stdin;
1	Rouge
2	Orange
3	Jaune
4	Vert
\.


--
-- Data for Name: user_; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.user_ (id_user, nom, email, telephone, adresse, mot_de_passe, id_fonction) FROM stdin;
2	Mahaliana Razafimanjato	mahalianarazafimanjato@gmail.com	+261382305086	Antsirabe	password123	3
3	Bakomalala Fenitra	bakomalalafenitra@gmail.com	+261383817421	Fianarantsoa	password123	1
4	Harimalala Ericka	harimalalaerickarandria@gmail.com	+261349322431	Toamasina	password123	3
6	RAZAFIMANANTSOA Tsiresy	mntsiresy@gmail.com	+261346367580	Antananarivo	responsable123	4
1	Romeo Esther	romeesther66@gmail.com	+261349833949	Antananarivo	password123	2
\.


--
-- Data for Name: userapp; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.userapp (iduserapp, motdepasse, login, id_patrouilleur) FROM stdin;
4	pass123	jean_r	5
5	pass456	marie_r	6
6	pass789	hery_a	7
7	pass012	agent	8
8	password4+2613463675804	agent10	10
\.


--
-- Name: alerte_id_alerte_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.alerte_id_alerte_seq', 26, true);


--
-- Name: fonction_id_fonction_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.fonction_id_fonction_seq', 4, true);


--
-- Name: historique_message_status_id_historique_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.historique_message_status_id_historique_seq', 32, true);


--
-- Name: historique_status_agent_id_historique_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.historique_status_agent_id_historique_seq', 1, false);


--
-- Name: intervention_id_intervention_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.intervention_id_intervention_seq', 3, true);


--
-- Name: message_id_message_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.message_id_message_seq', 66, true);


--
-- Name: message_patrouilleur_id_message_patrouilleur_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.message_patrouilleur_id_message_patrouilleur_seq', 1, false);


--
-- Name: patrouilleurs_id_patrouilleur_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.patrouilleurs_id_patrouilleur_seq', 10, true);


--
-- Name: site_id_site_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.site_id_site_seq', 4, true);


--
-- Name: status_agent_id_status_agent_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.status_agent_id_status_agent_seq', 3, true);


--
-- Name: status_message_id_status_message_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.status_message_id_status_message_seq', 3, true);


--
-- Name: typealerte_id_typealerte_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.typealerte_id_typealerte_seq', 4, true);


--
-- Name: user__id_user_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.user__id_user_seq', 12, true);


--
-- Name: userapp_iduserapp_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.userapp_iduserapp_seq', 8, true);


--
-- Name: alerte alerte_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.alerte
    ADD CONSTRAINT alerte_pkey PRIMARY KEY (id_alerte);


--
-- Name: fonction fonction_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fonction
    ADD CONSTRAINT fonction_pkey PRIMARY KEY (id_fonction);


--
-- Name: fonction_zonealerte fonction_zonealerte_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fonction_zonealerte
    ADD CONSTRAINT fonction_zonealerte_pkey PRIMARY KEY (id_fonction, id_typealerte);


--
-- Name: historique_message_status historique_message_status_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.historique_message_status
    ADD CONSTRAINT historique_message_status_pkey PRIMARY KEY (id_historique);


--
-- Name: historique_status_agent historique_status_agent_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.historique_status_agent
    ADD CONSTRAINT historique_status_agent_pkey PRIMARY KEY (id_historique);


--
-- Name: intervention intervention_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.intervention
    ADD CONSTRAINT intervention_pkey PRIMARY KEY (id_intervention);


--
-- Name: message_patrouilleur message_patrouilleur_id_patrouilleur_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.message_patrouilleur
    ADD CONSTRAINT message_patrouilleur_id_patrouilleur_key UNIQUE (id_patrouilleur);


--
-- Name: message_patrouilleur message_patrouilleur_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.message_patrouilleur
    ADD CONSTRAINT message_patrouilleur_pkey PRIMARY KEY (id_message_patrouilleur);


--
-- Name: message message_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.message
    ADD CONSTRAINT message_pkey PRIMARY KEY (id_message);


--
-- Name: patrouilleurs patrouilleurs_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.patrouilleurs
    ADD CONSTRAINT patrouilleurs_pkey PRIMARY KEY (id_patrouilleur);


--
-- Name: site site_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.site
    ADD CONSTRAINT site_pkey PRIMARY KEY (id_site);


--
-- Name: status_agent status_agent_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.status_agent
    ADD CONSTRAINT status_agent_pkey PRIMARY KEY (id_status_agent);


--
-- Name: status_agent status_agent_status_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.status_agent
    ADD CONSTRAINT status_agent_status_key UNIQUE (status);


--
-- Name: status_message status_message_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.status_message
    ADD CONSTRAINT status_message_pkey PRIMARY KEY (id_status_message);


--
-- Name: status_message status_message_status_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.status_message
    ADD CONSTRAINT status_message_status_key UNIQUE (status);


--
-- Name: typealerte typealerte_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.typealerte
    ADD CONSTRAINT typealerte_pkey PRIMARY KEY (id_typealerte);


--
-- Name: typealerte typealerte_zone_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.typealerte
    ADD CONSTRAINT typealerte_zone_key UNIQUE (zone);


--
-- Name: patrouilleurs uniq_nom_par_site; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.patrouilleurs
    ADD CONSTRAINT uniq_nom_par_site UNIQUE (id_site, nom);


--
-- Name: user_ user__email_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_
    ADD CONSTRAINT user__email_key UNIQUE (email);


--
-- Name: user_ user__pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_
    ADD CONSTRAINT user__pkey PRIMARY KEY (id_user);


--
-- Name: userapp userapp_id_patrouilleur_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.userapp
    ADD CONSTRAINT userapp_id_patrouilleur_key UNIQUE (id_patrouilleur);


--
-- Name: userapp userapp_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.userapp
    ADD CONSTRAINT userapp_pkey PRIMARY KEY (iduserapp);


--
-- Name: patrouilleurs trig_check_tel; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER trig_check_tel BEFORE INSERT OR UPDATE ON public.patrouilleurs FOR EACH ROW EXECUTE FUNCTION public.check_telephone_sites();


--
-- Name: patrouilleurs trig_sync_responsable; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER trig_sync_responsable AFTER INSERT ON public.patrouilleurs FOR EACH ROW EXECUTE FUNCTION public.sync_responsable_to_user();


--
-- Name: alerte alerte_id_message_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.alerte
    ADD CONSTRAINT alerte_id_message_fkey FOREIGN KEY (id_message) REFERENCES public.message(id_message);


--
-- Name: alerte alerte_id_site_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.alerte
    ADD CONSTRAINT alerte_id_site_fkey FOREIGN KEY (id_site) REFERENCES public.site(id_site);


--
-- Name: alerte alerte_id_typealerte_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.alerte
    ADD CONSTRAINT alerte_id_typealerte_fkey FOREIGN KEY (id_typealerte) REFERENCES public.typealerte(id_typealerte);


--
-- Name: fonction_zonealerte fk6x3sssh3lgp5yq8aj1n7121x9; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fonction_zonealerte
    ADD CONSTRAINT fk6x3sssh3lgp5yq8aj1n7121x9 FOREIGN KEY (id_fonction) REFERENCES public.fonction(id_fonction);


--
-- Name: historique_message_status fk_message; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.historique_message_status
    ADD CONSTRAINT fk_message FOREIGN KEY (id_message) REFERENCES public.message(id_message);


--
-- Name: historique_message_status fk_status; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.historique_message_status
    ADD CONSTRAINT fk_status FOREIGN KEY (id_status) REFERENCES public.status_message(id_status_message);


--
-- Name: fonction_zonealerte fkvkhjq398yybr705es83met8i; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fonction_zonealerte
    ADD CONSTRAINT fkvkhjq398yybr705es83met8i FOREIGN KEY (id_typealerte) REFERENCES public.typealerte(id_typealerte);


--
-- Name: historique_status_agent historique_status_agent_id_patrouilleur_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.historique_status_agent
    ADD CONSTRAINT historique_status_agent_id_patrouilleur_fkey FOREIGN KEY (id_patrouilleur) REFERENCES public.patrouilleurs(id_patrouilleur);


--
-- Name: historique_status_agent historique_status_agent_id_status_agent_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.historique_status_agent
    ADD CONSTRAINT historique_status_agent_id_status_agent_fkey FOREIGN KEY (id_status_agent) REFERENCES public.status_agent(id_status_agent);


--
-- Name: message message_id_intervention_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.message
    ADD CONSTRAINT message_id_intervention_fkey FOREIGN KEY (id_intervention) REFERENCES public.intervention(id_intervention);


--
-- Name: message message_iduserapp_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.message
    ADD CONSTRAINT message_iduserapp_fkey FOREIGN KEY (iduserapp) REFERENCES public.userapp(iduserapp);


--
-- Name: message_patrouilleur message_patrouilleur_id_message_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.message_patrouilleur
    ADD CONSTRAINT message_patrouilleur_id_message_fkey FOREIGN KEY (id_message) REFERENCES public.message(id_message);


--
-- Name: message_patrouilleur message_patrouilleur_id_patrouilleur_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.message_patrouilleur
    ADD CONSTRAINT message_patrouilleur_id_patrouilleur_fkey FOREIGN KEY (id_patrouilleur) REFERENCES public.patrouilleurs(id_patrouilleur);


--
-- Name: patrouilleurs patrouilleurs_id_site_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.patrouilleurs
    ADD CONSTRAINT patrouilleurs_id_site_fkey FOREIGN KEY (id_site) REFERENCES public.site(id_site);


--
-- Name: user_ user__id_fonction_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_
    ADD CONSTRAINT user__id_fonction_fkey FOREIGN KEY (id_fonction) REFERENCES public.fonction(id_fonction);


--
-- Name: userapp userapp_id_patrouilleur_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.userapp
    ADD CONSTRAINT userapp_id_patrouilleur_fkey FOREIGN KEY (id_patrouilleur) REFERENCES public.patrouilleurs(id_patrouilleur);


--
-- PostgreSQL database dump complete
--

