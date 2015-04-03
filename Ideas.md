
```
//the current way would be
source.clean_table("table_name", "email=\'gl@name.com\'" );

// this is how it might look like in next ver.
source.delete_records("table_name").all();
source.delete_records("table_name").where("id", 123).and("email", "gl@name.com").clean();
source.delete_records("table_name").where("id", 123).clean();

```