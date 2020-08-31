#!/bin/bash

changelog_location="fastlane/metadata/android/en-US/changelogs"
count_txt=`ls -r $changelog_location/*.txt | wc -l`
count_tag=`git tag | wc -l`

if (( count_txt != count_tag )); then
    echo "Tag count and txt-file count not matching."
    exit 1
fi

echo "# Changelog" > CHANGELOG.md

for i in `seq $count_txt -1 1`
do
   tag=`git tag | sed "$i!d"`

   echo "## $tag ($i)" >> CHANGELOG.md
   cat $changelog_location/$i.txt >> CHANGELOG.md
   
done
