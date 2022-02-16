#! /bin/sh

if [ $# -lt 1 ] ; then
  echo "Usage $(basename $0) <module>"
  exit 1
fi

module=$1
version=$(./gradlew :${module}:properties | grep '^version:' | sed -e 's/^version: \(.*\)$/v\1/')
tag="${module}_${version}"

# When GH Actions checks out the repo it doesn't pull tags
echo "Fetching tags"
git fetch --tags

echo "Checking for ${tag}"

if git show-ref --tags $tag --quiet; then
  echo "Tag exists"
else
  echo "Tagging with ${tag}"
  git tag $tag

  # Need to push to GH to then create a release
  git push --tags

  echo $GH_AUTH_TOKEN | gh auth login --with-token
  gh release create $tag
fi
