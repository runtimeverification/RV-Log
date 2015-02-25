The formula below is derived from the Monpoly style delete-1-2 formula by ignoring 
the fine-grained interval of temporal operator.

FORALL ?user . FORALL ?data . (delete(?user, db1, ?p, ?data) AND NOT(?data = "unknown")) IMPLIES 
(EVENTUALLY[0,*] EXISTS ?u2 . delete(?u2, db2, ?p, ?data)) OR 
( ONCE (EXISTS ?u2 . insert(?u2, db1, ?p, ?data)) AND
(PAST_ALWAYS (ALWAYS[0,*] NOT (EXISTS ?u2 . insert(?u2, db2, ?p, ?data) ) ) ) )

describes such a property:
Whenever some user deletes some data 'v' which is NOT unknown
in `database 1`, it implies that:
Either: sometime in the future there will be some user delete 
the same data 'v' in `database 2`;
Or: (some time in the past, some user inserted the data 'v' to `database 1`,
and there never existed, and will never be a user such that he/she inserted
to `database 2` the data 'v').

Monply cannot monitor such property because it uses unbounded future operator.
Output of Monpoly: "The formula contains an unbounded future temporal operator. It is hence not monitorable."

There is no way to check the value after slicing, so in the event action
method, data field is checked to eliminate the events that will not cause violations of the property.


