#!/bin/bash
# Erzeugt die Print-PDF mit WeasyPrint (CSS Paged Media).
# Voraussetzung:  brew install pandoc weasyprint
cd "$(dirname "$0")" || exit 1
pandoc Datalog-Handbuch.md \
  --template=template.html \
  --toc --toc-depth=2 \
  --pdf-engine=weasyprint \
  --pdf-engine-opt=-s --pdf-engine-opt=print.css \
  -o Datalog-Handbuch.pdf
echo "Fertig: Datalog-Handbuch.pdf"
echo "Zum Vorschauen im Browser:"
echo "  pandoc Datalog-Handbuch.md --template=template.html --toc --toc-depth=2 -c print.css --embed-resources -o preview.html"
