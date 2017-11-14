# cassandra-basics

## Business data model

A vessel has several attributes:
 - UUID: unique technical identifier
 - Name: display label, not necessarily unique
 - Category: _cargo_ or _windsurf_
 - Visibility: either "all centers", or "creation center only"
 - Creation center: ID of the center that created the vessel instance
 - Last departure port: ID of the port that the vessel last departed from (only applicable to category _cargo_)
 - Last departure time: time that the vessel departed from last port (only applicable to category _cargo_)

## Queries

 - READ: Get list of all vessels that are visible to site
 - READ: Get vessel from selected UUID
 - READ: Find vessels by name fragment (among vessels that are visible to site)
 - READ: Find vessels by category (among vessels that are visible to site)
 - READ: Get list of vessels that departed recently from a selected port
 - WRITE: Create vessel
 - WRITE: Update vessel departure info
 - WRITE: Change vessel visibility
 - WRITE: Delete vessel

## Physical data model

Namespace: vessel

### Get list of all vessels that are visible to site
 - Table name: vessels
 - Partitioning key(s): "_ALL" or creation center ID
 - Clustering key(s): UUID

A secondary index (SASI) is created against **name** attribute of **vessels** table, in order to be able to find rows that "contain" name fragments.

Materialized view **vessels_by_category** is created to request zones against **category** attribute of **vessels** table.

### Get vessel from selected UUID
 - Table name: vessels_by_uuid
 - Partitioning key(s): UUID
 - Clustering key(s): (none)

### Get list of vessels that departed recently from a selected port
 - Table name: vessels_by_departure_port
 - Partitioning key(s): last departure port
 - Clustering key(s): UUID

Only vessels with category _cargo_ are inserted in this table.

A TTL is set in order for the content of this table to be removed when it is not relevant anymore.