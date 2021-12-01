#!/bin/bash
# fetch-secrets.sh fetch account secrets

work=$( pwd )
names="admin gitlab cloudsmith trigger"
for secret in $names; do
    gcloud secrets versions access latest \
        --secret=hoot-secret-$secret --format='get(payload.data)' \
        | tr '_-' '/+' | base64 -d > $work/hoot-secret-$secret.txt
done

ls -al $work/hoot-secret*
