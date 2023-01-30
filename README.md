# lastfm-tracks-dumper

This cmd application obtains listened (aka *scrobbled*) tracks for a specific [Last.fm](https://www.last.fm/home) user 
and save it to .csv file.

### Usage 

Run the .jar and pass two parameters as the arguments:

1. Required Last.fm username
2. Last.fm API token, see [here](https://www.last.fm/api#getting-started).

```shell

java -jar lastfm-tracks-dumper-0.0.1-standalone.jar 'username' 'api_token'
```

Currently only `date`, `artist`, `track` and `album` fields are saved to .csv.
