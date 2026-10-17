param([string]$Output = "sample-data/inventory-10000.csv")

$folder = Split-Path -Parent $Output
if ($folder) { New-Item -ItemType Directory -Force -Path $folder | Out-Null }
$writer = [System.IO.StreamWriter]::new($Output, $false, [System.Text.UTF8Encoding]::new($false))
try {
    $writer.WriteLine("entryCode,name,batchNumber,quantity,price,expiryDate")
    for ($index = 1; $index -le 10000; $index++) {
        $writer.WriteLine(("MED-{0:D5},Medicine {0},BATCH-{0:D5},{1},{2:F2},2030-12-31" -f $index, ($index % 500), (10 + ($index % 90) / 10.0)))
    }
} finally { $writer.Dispose() }
Write-Host "Generated $Output with 10,000 valid inventory records."
