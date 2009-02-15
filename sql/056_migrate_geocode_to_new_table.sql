insert into geocode (address)
select distinct geocode as geocode from resource where geocode IS NOT NULL;

update resource, geocode 
set resource.geocode_id = geocode.id
WHERE resource.geocode = geocode.address;

