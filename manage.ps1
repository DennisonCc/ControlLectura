# Script de ayuda para gestionar el sistema de microservicios

# Colores para output
$Green = "Green"
$Yellow = "Yellow"
$Red = "Red"
$Cyan = "Cyan"

function Show-Menu {
    Write-Host "`n========================================" -ForegroundColor $Cyan
    Write-Host "  E-Commerce Microservices Manager" -ForegroundColor $Cyan
    Write-Host "========================================`n" -ForegroundColor $Cyan
    
    Write-Host "1. Construir imágenes Docker" -ForegroundColor $Green
    Write-Host "2. Iniciar sistema completo" -ForegroundColor $Green
    Write-Host "3. Detener sistema" -ForegroundColor $Yellow
    Write-Host "4. Ver logs de todos los servicios" -ForegroundColor $Green
    Write-Host "5. Ver logs de Order Service" -ForegroundColor $Green
    Write-Host "6. Ver logs de Inventory Service" -ForegroundColor $Green
    Write-Host "7. Ver estado de servicios" -ForegroundColor $Green
    Write-Host "8. Probar flujo completo" -ForegroundColor $Cyan
    Write-Host "9. Limpiar sistema (eliminar volúmenes)" -ForegroundColor $Red
    Write-Host "0. Salir`n" -ForegroundColor $Yellow
}

function Build-Images {
    Write-Host "`nConstruyendo imágenes Docker..." -ForegroundColor $Cyan
    docker-compose build
}

function Start-System {
    Write-Host "`nIniciando sistema completo..." -ForegroundColor $Cyan
    docker-compose up -d
    Write-Host "`nEsperando a que los servicios estén listos..." -ForegroundColor $Yellow
    Start-Sleep -Seconds 10
    docker-compose ps
}

function Stop-System {
    Write-Host "`nDeteniendo sistema..." -ForegroundColor $Yellow
    docker-compose down
}

function Show-Logs {
    Write-Host "`nMostrando logs (Ctrl+C para salir)..." -ForegroundColor $Cyan
    docker-compose logs -f
}

function Show-OrderLogs {
    Write-Host "`nMostrando logs de Order Service (Ctrl+C para salir)..." -ForegroundColor $Cyan
    docker-compose logs -f order-service
}

function Show-InventoryLogs {
    Write-Host "`nMostrando logs de Inventory Service (Ctrl+C para salir)..." -ForegroundColor $Cyan
    docker-compose logs -f inventory-service
}

function Show-Status {
    Write-Host "`nEstado de servicios:" -ForegroundColor $Cyan
    docker-compose ps
}

function Test-Flow {
    Write-Host "`n========================================" -ForegroundColor $Cyan
    Write-Host "  Probando Flujo Completo" -ForegroundColor $Cyan
    Write-Host "========================================`n" -ForegroundColor $Cyan
    
    Write-Host "1. Verificando stock disponible..." -ForegroundColor $Green
    $productId = "a3c2b1d0-6b0e-4f2b-9c1a-2d3f4a5b6c7d"
    curl "http://localhost:8081/api/v1/products/$productId/stock"
    
    Write-Host "`n`n2. Creando pedido..." -ForegroundColor $Green
    $orderRequest = @{
        customerId = "9f7a1e2a-31f6-4a53-b0d2-6f4f1c7a3b2e"
        items = @(
            @{
                productId = "a3c2b1d0-6b0e-4f2b-9c1a-2d3f4a5b6c7d"
                quantity = 2
            }
        )
        shippingAddress = @{
            country = "EC"
            city = "Quito"
            street = "Av. Amazonas"
            postalCode = "170135"
        }
        paymentReference = "pay_test123"
    } | ConvertTo-Json
    
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/orders" `
        -Method Post `
        -ContentType "application/json" `
        -Body $orderRequest
    
    $orderId = $response.orderId
    Write-Host "`nPedido creado: $orderId" -ForegroundColor $Yellow
    Write-Host "Estado inicial: $($response.status)" -ForegroundColor $Yellow
    
    Write-Host "`n3. Esperando procesamiento asíncrono..." -ForegroundColor $Green
    Start-Sleep -Seconds 3
    
    Write-Host "`n4. Consultando estado final del pedido..." -ForegroundColor $Green
    curl "http://localhost:8080/api/v1/orders/$orderId"
    
    Write-Host "`n`n5. Verificando stock actualizado..." -ForegroundColor $Green
    curl "http://localhost:8081/api/v1/products/$productId/stock"
    
    Write-Host "`n`nRabbitMQ Management UI: http://localhost:15672" -ForegroundColor $Cyan
    Write-Host "Usuario: guest | Password: guest`n" -ForegroundColor $Cyan
}

function Clean-System {
    Write-Host "`n¿Estás seguro de eliminar todos los volúmenes? (S/N): " -ForegroundColor $Red -NoNewline
    $confirm = Read-Host
    if ($confirm -eq "S" -or $confirm -eq "s") {
        Write-Host "`nLimpiando sistema y eliminando volúmenes..." -ForegroundColor $Red
        docker-compose down -v
        Write-Host "Sistema limpiado." -ForegroundColor $Green
    } else {
        Write-Host "Operación cancelada." -ForegroundColor $Yellow
    }
}

# Main loop
do {
    Show-Menu
    $option = Read-Host "Selecciona una opción"
    
    switch ($option) {
        "1" { Build-Images }
        "2" { Start-System }
        "3" { Stop-System }
        "4" { Show-Logs }
        "5" { Show-OrderLogs }
        "6" { Show-InventoryLogs }
        "7" { Show-Status }
        "8" { Test-Flow }
        "9" { Clean-System }
        "0" { 
            Write-Host "`n¡Hasta luego!`n" -ForegroundColor $Cyan
            exit 
        }
        default { 
            Write-Host "`nOpción inválida. Intenta de nuevo." -ForegroundColor $Red 
        }
    }
    
    if ($option -ne "0") {
        Write-Host "`nPresiona Enter para continuar..." -ForegroundColor $Yellow
        Read-Host
    }
} while ($option -ne "0")
