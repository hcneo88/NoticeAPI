
CREATE TABLE CM_LetterHead  
    (   letterhead_id    CHAR(20),
        letterhead_title VARCHAR(256),
        revision_no      INT NOT NULL,
        created_by       VARCHAR(30) NOT NULL DEFAULT 'SYSTEM',
        created_date     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_by       VARCHAR(30) NOT NULL DEFAULT 'SYSTEM',
        updated_date        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        version_no       INT NOT NULL DEFAULT 0,
        PRIMARY KEY (letterhead_id)
    );

-- When there is a staff movement,  the physical file containing the signature should be replaced
-- since the departed staff is no longer an authorized signatories
CREATE TABLE CM_Signatory  
    (   signatory_id        CHAR(20),                               
        signatory_title     VARCHAR(128),
        signoff_text        VARCHAR(25) DEFAULT 'Your faithfully',            
        designation_line1   VARCHAR(60)  NOT NULL DEFAULT '',
        designation_line2   VARCHAR(60)  NOT NULL DEFAULT '',  
        designation_line3   VARCHAR(60)  NOT NULL DEFAULT '', 
        revision_no         INT NOT NULL,
        created_by          VARCHAR(30) NOT NULL DEFAULT 'SYSTEM',
        created_date        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_by          VARCHAR(30) NOT NULL DEFAULT 'SYSTEM',
        updated_date        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        version_no          INT NOT NULL DEFAULT 0,
        PRIMARY KEY (signatory_id)
    );



-- recipeint type 
-- 0 : No Name, No address
-- 1 : Name, no address
-- 2 : No name, with 3 address lines
-- 3 : Name, with 3 address Lines

-- print type
-- L : print locally only
-- B : print online and outsource
-- O : outsource only

CREATE TABLE CM_Notices   
    (   notice_id           CHAR(20),
        notice_num          CHAR(10) NOT NULL,
        notice_title        VARCHAR(256) NOT NULL,   
        recipient_type_cd   INT DEFAULT 1,  
        print_type_cd       CHAR(1) DEFAULT 'B',  
        revision_no         INT NOT NULL,       
        letterhead_id       CHAR(20),
        signatory_id        CHAR(20),                                 -- Z : no signature
        eff_start_date      TIMESTAMP NOT NULL,
        eff_end_date        TIMESTAMP DEFAULT '2038-01-19 00:00:00',   -- Max date allowed by mySQL
        created_by          VARCHAR(30) NOT NULL DEFAULT 'SYSTEM',
        created_date        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_by          VARCHAR(30) NOT NULL DEFAULT 'SYSTEM',
        updated_date        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        version_no          INT NOT NULL DEFAULT 0,
        PRIMARY KEY (notice_id),
        UNIQUE KEY (notice_num, eff_end_date),
        CONSTRAINT fk_cm_letterhead
            FOREIGN KEY (letterhead_id) 
            REFERENCES CM_LetterHead(letterhead_id),
        CONSTRAINT fk_cm_signatory
            FOREIGN KEY (signatory_id) 
            REFERENCES CM_Signatory(signatory_id)
    );


-- Field Types
-- First characters: (a) A : ASCII,    (b) I : Image   (c) Q : QR code, (c) T : Table field
-- Second character (text ascii + table + QR ):  (a) S : String (b) N : Number (c) F : Float (d) D : Date 
    

CREATE TABLE CM_NoticeFields   
    (   field_id            CHAR(20) NOT NULL,
        notice_id           CHAR(20) NOT NULL,
        field_name          CHAR(20) NOT NULL,
        field_type_cd       CHAR(2)  NOT NULL DEFAULT 'AS',   
        mandatory_cd        CHAR(1)  NOT NULL DEFAULT 'N',         
        created_by          VARCHAR(30) NOT NULL DEFAULT 'SYSTEM',
        created_date        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_by          VARCHAR(30) NOT NULL DEFAULT 'SYSTEM',
        updated_date        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        version_no          INT NOT NULL DEFAULT 0,
        PRIMARY KEY (field_id),
        CONSTRAINT fk_cm_noticefield1
            FOREIGN KEY (notice_id) 
            REFERENCES CM_Notices(notice_id)
    );




CREATE TABLE CM_NoticeInstances  
    (   instance_id           CHAR(20), 
        instance_date         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
        notice_id             CHAR(20) NOT NULL, 
        notice_num            CHAR(10) NOT NULL, 
        letterhead_id         CHAR(20) NOT NULL,            -- purposely denormlized.  Snanshort
        signatory_id          CHAR(20) NOT NULL,            -- purposely denormalized. Snapshot
        print_type_cd         CHAR(1) NOT NULL DEFAULT 'B',
        outsource_action_cd   CHAR(1) NOT NULL DEFAULT '',   -- Valid only for print_type_cd = B or O. Value S to send for outsource,  H - Hold
        outsource_status_cd   CHAR(1) NOT NULL DEFAULT '',   -- P printed by ousource, G - generated mergefield to outsource
        outsource_print_date  TIMESTAMP,
        local_print_date      TIMESTAMP,
        recipient_name        VARCHAR(66),
        addr_type_cd          CHAR(1) DEFAULT 'S',  
        blk_hse_num           CHAR(5) DEFAULT '', 
        streetName            VARCHAR(32) DEFAULT '', 
        floor_num             CHAR(3) DEFAULT '',
        unit_num              CHAR(5) DEFAULT '',
        bldg_name             VARCHAR(30) DEFAULT '',
        postal_cd             CHAR(6) DEFAULT '',
        merge_fields          MEDIUMTEXT NOT NULL,
        headerfooter_fields   MEDIUMTEXT NOT NULL, 
        created_by            VARCHAR(30) NOT NULL DEFAULT 'SYSTEM',
        created_date          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_by            VARCHAR(30) NOT NULL DEFAULT 'SYSTEM',
        updated_date          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        version_no            INT NOT NULL DEFAULT 0,
        PRIMARY KEY (instance_id),
        INDEX  (recipient_name),
        INDEX  (notice_num, recipient_name),
        CONSTRAINT fk_cm_noticeinstance1
            FOREIGN KEY (notice_id) 
            REFERENCES CM_Notices(notice_id),
        CONSTRAINT fk_cm_noticeinstance2
            FOREIGN KEY (letterhead_id) 
            REFERENCES CM_LetterHead(letterhead_id),
        CONSTRAINT fk_cm_noticeinstance3
            FOREIGN KEY (signatory_id) 
            REFERENCES CM_Signatory(signatory_id)
    );


