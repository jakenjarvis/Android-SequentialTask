git subtree pull --prefix=repository git@github.com:jakenjarvis/Android-SequentialTask.git gh-pages --squash

call gradlew clean build publish

cd repository

git add .
git commit -m "update repository"

cd ..

git subtree push --prefix=repository git@github.com:jakenjarvis/Android-SequentialTask.git gh-pages
git push origin master

pause
