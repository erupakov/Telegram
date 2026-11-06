#!/usr/bin/env python3
"""Builds TMessagesProj/src/main/assets/cities.txt from GeoNames dumps.

Inputs (https://download.geonames.org/export/dump/), unpacked into one directory:
  cities1000.txt            all places with 1000+ people
  alternatenames/XX.zip     per-country alternate names with language tags

Which places go in:
  - every place with 15000+ people, plus capitals and region centres (PPLC, PPLA);
  - in countries with fewer than SMALL_COUNTRY_CITIES such places (Armenia, Afghanistan, ...)
    every place from cities1000, so their towns can be found too.

Output: the 19 GeoNames columns, plus a 20th column with the Russian name
(the preferred "ru" name, else the first one that is not short, colloquial or historic).
GeoNames alternate names have no language tags, so without it the app picked
"Дубаи" (Serbian/Bulgarian) over "Дубай".

Usage: build_cities.py <geonames dir> <output cities.txt>
"""
import collections
import glob
import os
import sys
import zipfile

SMALL_COUNTRY_CITIES = 100
MIN_POPULATION = 15000
ALWAYS_FEATURES = {'PPLC', 'PPLA'}


def population(row):
    return int(row[14] or 0)


def ru_names(geo_dir, ids):
    names = collections.defaultdict(list)
    for path in sorted(glob.glob(os.path.join(geo_dir, 'alternatenames', '*.zip'))):
        country = os.path.basename(path)[:-4]
        with zipfile.ZipFile(path) as archive:
            if country + '.txt' not in archive.namelist():
                continue
            with archive.open(country + '.txt') as f:
                for raw in f:
                    # alternateNameId, geonameid, isolanguage, name, isPreferred, isShort, isColloquial, isHistoric, from, to
                    p = raw.decode('utf-8').rstrip('\n').split('\t')
                    if len(p) < 4 or p[2] != 'ru' or p[1] not in ids:
                        continue
                    flag = lambda i: len(p) > i and p[i] == '1'
                    names[p[1]].append((p[3], flag(4), flag(5) or flag(6) or flag(7)))
    best = {}
    for geoname_id, candidates in names.items():
        usable = [c for c in candidates if not c[2]] or candidates
        preferred = [c for c in usable if c[1]]
        best[geoname_id] = (preferred or usable)[0][0]
    return best


def main(geo_dir, output):
    with open(os.path.join(geo_dir, 'cities1000.txt'), encoding='utf-8') as f:
        rows = [line.rstrip('\n').split('\t') for line in f]
    by_country = collections.defaultdict(list)
    for row in rows:
        by_country[row[8]].append(row)

    selected = []
    for country in sorted(by_country):
        places = by_country[country]
        big = [r for r in places if population(r) >= MIN_POPULATION]
        if len(big) < SMALL_COUNTRY_CITIES:
            selected += places
        else:
            selected += [r for r in places if population(r) >= MIN_POPULATION or r[7] in ALWAYS_FEATURES]

    russian = ru_names(geo_dir, {r[0] for r in selected})
    with open(output, 'w', encoding='utf-8', newline='\n') as out:
        for row in selected:
            out.write('\t'.join(row[:19] + [russian.get(row[0], '')]) + '\n')
    print(f'{len(selected)} places, {sum(1 for r in selected if r[0] in russian)} with a Russian name')


if __name__ == '__main__':
    main(sys.argv[1], sys.argv[2])
