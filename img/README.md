# Images and Multimedia

- `screenshots/`: screenshots linked from the root README.
- `web/`: card images, textures and backgrounds used by the app.

Maven copies `web/` to `static/images` in the JAR; browser URLs `/images/...` remain the same.
For runtime changes, run Maven from the repository root with `./code/mvnw -f code/pom.xml ...`.
