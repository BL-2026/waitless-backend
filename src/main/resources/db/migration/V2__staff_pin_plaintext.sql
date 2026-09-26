ALTER TABLE staff_member ADD COLUMN pin varchar(4);

UPDATE staff_member s
SET pin = lpad(t.rn::text, 4, '0')
FROM (
    SELECT id, row_number() OVER (PARTITION BY store_id ORDER BY full_name) AS rn
    FROM staff_member
) t
WHERE s.id = t.id;

ALTER TABLE staff_member ALTER COLUMN pin SET NOT NULL;
ALTER TABLE staff_member DROP COLUMN pin_hash;

ALTER TABLE staff_member ADD CONSTRAINT uq_staff_member_store_pin UNIQUE (store_id, pin);
