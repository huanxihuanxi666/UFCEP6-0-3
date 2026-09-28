$word = New-Object -ComObject Word.Application
$word.Visible = $false
$docx = (Resolve-Path 'output/Hospital总图与五人子图逻辑及Java代码说明.docx').Path
$pdf = Join-Path (Resolve-Path '.presentation_work').Path 'logic_code_reference.pdf'
$doc = $word.Documents.Open($docx)
$doc.SaveAs2($pdf, 17)
$doc.Close()
$word.Quit()
Remove-Item -LiteralPath '.presentation_work/logic_code_pages' -Recurse -Force -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Path '.presentation_work/logic_code_pages' | Out-Null
& pdftoppm -png -r 100 '.presentation_work/logic_code_reference.pdf' '.presentation_work/logic_code_pages/page'
