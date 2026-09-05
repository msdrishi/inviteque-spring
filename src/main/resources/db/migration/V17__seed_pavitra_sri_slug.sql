-- Set slug on the existing Pavitra-Sri invite identified by its UUID
UPDATE invites 
SET slug = 'Pavitra-Sri' 
WHERE id = '7db34071-0d3f-422e-ab65-cae172b84e43'
  AND slug IS NULL;
