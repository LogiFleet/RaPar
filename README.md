# RaPar

Raw data Parser for Telematics Device

e.g. launch arguments for fmc130:

- STD:
tlt.fmc130 tlt.fmc130.avlid tlt.fmc130.avlid.description -ts

- SCP raw data file from remote server:
tlt.fmc130 tlt.fmc130.avlid tlt.fmc130.avlid.description user host port rsaPublicKeyFilePath rsaPublicKeyFilePassword("null" if none) tlt.fmc130.rawdata.int.live.remote.folder tlt.fmc130.rawdata.int.live.remote.file tlt.fmc130.rawdata.int.live.local.folder -ts

-ts = timestamp
