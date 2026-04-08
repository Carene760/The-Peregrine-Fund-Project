--
-- PostgreSQL database dump
--

\restrict gw5HDnqjR5jgmFJJGV7yBqmszYI0mC1CuBpmc5pZH1wj1siGm4QzwC6hnXxHBZp

-- Dumped from database version 18.1
-- Dumped by pg_dump version 18.1

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: public; Type: SCHEMA; Schema: -; Owner: pg_database_owner
--

CREATE SCHEMA public;


ALTER SCHEMA public OWNER TO pg_database_owner;

--
-- Name: SCHEMA public; Type: COMMENT; Schema: -; Owner: pg_database_owner
--

COMMENT ON SCHEMA public IS 'standard public schema';


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

\unrestrict gw5HDnqjR5jgmFJJGV7yBqmszYI0mC1CuBpmc5pZH1wj1siGm4QzwC6hnXxHBZp

