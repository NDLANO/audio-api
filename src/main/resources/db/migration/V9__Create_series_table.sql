CREATE TABLE seriesdata (
  id BIGSERIAL PRIMARY KEY,
  revision integer not null default 1,
  document JSONB
);

ALTER TABLE audiodata ADD COLUMN series_ids BIGSERIAL REFERENCES seriesdata(id);
