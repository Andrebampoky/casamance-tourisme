#!/usr/bin/env python3
"""Render the project Markdown report to styled HTML for LibreOffice DOCX/PDF export."""
from html import escape
from pathlib import Path
import re

HERE = Path(__file__).resolve().parent
SOURCE = HERE / 'rapport.md'
OUTPUT = HERE / 'rapport.html'

def inline(value: str) -> str:
    value = escape(value)
    value = re.sub(r'\*\*(.+?)\*\*', r'<strong>\1</strong>', value)
    value = re.sub(r'`(.+?)`', r'<code>\1</code>', value)
    value = re.sub(r'\[([^\]]+)\]\(([^)]+)\)', r'<a href="\2">\1</a>', value)
    return value

lines = SOURCE.read_text(encoding='utf-8').splitlines()
out = []
i = 0
list_open = None
in_code = False
code = []
def close_list():
    global list_open
    if list_open:
        out.append(f'</{list_open}>')
        list_open = None

while i < len(lines):
    line = lines[i]
    if line.startswith('```'):
        close_list()
        if in_code:
            out.append('<pre><code>' + escape('\n'.join(code)) + '</code></pre>')
            code=[]; in_code=False
        else: in_code=True
        i+=1; continue
    if in_code:
        code.append(line); i+=1; continue
    if line.startswith('|') and i+1 < len(lines) and re.match(r'^\|[\s|:-]+\|\s*$', lines[i+1]):
        close_list()
        headers=[x.strip() for x in line.strip('|').split('|')]
        out.append('<table><thead><tr>'+''.join('<th>'+inline(x)+'</th>' for x in headers)+'</tr></thead><tbody>')
        i+=2
        while i < len(lines) and lines[i].startswith('|'):
            cells=[x.strip() for x in lines[i].strip('|').split('|')]
            out.append('<tr>'+''.join('<td>'+inline(x)+'</td>' for x in cells)+'</tr>')
            i+=1
        out.append('</tbody></table>'); continue
    if not line.strip(): close_list(); i+=1; continue
    if line.startswith('# '):
        close_list(); out.append('<h1>'+inline(line[2:])+'</h1>')
    elif line.startswith('## '):
        close_list(); title=line[3:]
        if title.startswith(('1. ','5. ','9. ','14. ','20. ','25. ')): out.append('<div class="pagebreak"></div>')
        out.append('<h2>'+inline(title)+'</h2>')
        if title.startswith('5. '): out.append('<figure><img src="../uml/architecture.svg"/><figcaption>Figure 1 — Architecture microservices déployée</figcaption></figure>')
        if title.startswith('9. '): out.append('<figure><img src="../uml/domain.svg"/><figcaption>Figure 2 — Modèle de données et associations.</figcaption></figure>')
        if title.startswith('15. '): out.append('<figure><img src="../uml/reservation-sequence.svg"/><figcaption>Figure 3 — Séquence simplifiée de réservation et confirmation.</figcaption></figure>')
    elif line.startswith('### '): close_list(); out.append('<h3>'+inline(line[4:])+'</h3>')
    elif line.startswith('> '): close_list(); out.append('<blockquote>'+inline(line[2:])+'</blockquote>')
    elif re.match(r'^\d+\. ',line):
        if list_open != 'ol': close_list(); list_open='ol'; out.append('<ol>')
        out.append('<li>'+inline(re.sub(r'^\d+\. ','',line))+'</li>')
    elif line.startswith('- '):
        if list_open != 'ul': close_list(); list_open='ul'; out.append('<ul>')
        out.append('<li>'+inline(line[2:])+'</li>')
    else: close_list(); out.append('<p>'+inline(line)+'</p>')
    i+=1
close_list()
html='''<!DOCTYPE html><html lang="fr"><head><meta charset="utf-8"><title>Rapport — Casamance Tourisme</title><style>
@page { size:A4; margin:2.1cm 1.9cm 2cm; }
body { font-family: Carlito, Calibri, sans-serif; color:#202820; font-size:10.5pt; line-height:1.42; }
h1 { font-size:30pt; color:#164b30; text-align:center; margin-top:110pt; }
h1 + h2 { text-align:center; border:0; font-size:18pt; color:#293c30; margin-bottom:60pt; }
h2 { color:#165c39; font-size:16pt; border-bottom:1px solid #a7c6b2; padding-bottom:5pt; margin-top:20pt; }
h3 { color:#24523a; font-size:12pt; }
p { text-align:justify; margin:5pt 0 9pt; }
li { margin:3pt 0; }
blockquote { background:#edf5ef; border-left:4px solid #28724a; padding:9pt; }
code,pre { font-family:DejaVu Sans Mono, monospace; font-size:9pt; background:#f1f3f2; }
pre { padding:10pt; white-space:pre-wrap; }
table { border-collapse:collapse; width:100%; font-size:9pt; margin:10pt 0; }
th { background:#1c5a39; color:white; }
th,td { border:1px solid #aebcb2; padding:5pt; vertical-align:top; }
figure { text-align:center; margin:12pt 0; page-break-inside:avoid; }
figure img { width:95%; height:auto; }
figcaption { color:#526256; font-size:9pt; font-style:italic; }
a { color:#165c39; }
.pagebreak { page-break-before:always; }
</style></head><body>'''+''.join(out)+'</body></html>'
OUTPUT.write_text(html, encoding='utf-8')
print(OUTPUT)
