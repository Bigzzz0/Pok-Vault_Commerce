*** Settings ***
Documentation     One real order, two isolated browser sessions. No API writes, no automatic rebooking.
Library           Browser    auto_closing_level=MANUAL
Library           OperatingSystem
Library           Collections
Library           String
Suite Setup       Prepare Two Sessions
Suite Teardown    Finish Sessions
Test Teardown     Finish Demo

*** Variables ***
${BASE_URL}       https://pok-vault-commerce.onrender.com
${CUSTOMER}       customer_red
${SELLER}         staff_ash
${CARD_NAME}      ${EMPTY}
${FRIEND_ID}      1234-5678-9012-3456
${MODE}           manual
${SPEED}          1.5
${HEADLESS}       ${False}
${STEP_TIMEOUT}   30s
${LOGIN_TIMEOUT}  180s
${CUE_TIMEOUT}    15 minutes
${RECORD_VIDEO}   ${False}
${FULLSCREEN}     ${True}
${ORDER_ID}       ${None}

*** Test Cases ***
Customer Books Seller Fulfils Customer Sees Result
    Switch Page    ${CUSTOMER_PAGE}
    Go To    ${BASE_URL}/cards
    Cue    01 ลูกค้าเลือกการ์ด    18
    Fill Text    input[name="search"]    ${PLAN}[cardName]
    Click    .gallery-search-submit
    Wait For Elements State    .tcg-card-3d[data-inventory-id="${PLAN}[inventoryId]"]    visible
    Scroll To Element    .tcg-card-3d[data-inventory-id="${PLAN}[inventoryId]"]
    Click    .tcg-card-3d[data-inventory-id="${PLAN}[inventoryId]"]
    Get Text    \#inspectCardName    ==    ${PLAN}[cardName]
    Cue    02 รายละเอียดและสต็อก    15
    Click    \#inspectionModal button[onclick="openCustomerOrderModal()"]
    Fill Text    \#customerOrderFriendId    ${FRIEND_ID}
    Fill Text    \#customerOrderIgn    ${DEMO_IGN}
    Cue    03 ยืนยันจองหนึ่งใบ    15
    # Exactly one booking click. A timeout stops the demo; it never retries the POST.
    Click    \#customerOrderSubmit
    Wait For Elements State    \#customerOrderDone    visible
    ${code}=    Get Text    \#customerOrderDoneCode
    ${code}=    Remove String    ${code}    \#
    Set Suite Variable    ${ORDER_CODE}    ${code}
    Switch Page    ${SELLER_PAGE}
    ${order}=    Demo Data    orderByCode    code=${ORDER_CODE}
    Set Suite Variable    ${ORDER_ID}    ${order}[id]
    Set Suite Variable    ${BOOKING}    ${order}
    Should Be Equal As Integers    ${order}[userId]    ${CUSTOMER_ID}
    Should Be Equal    ${order}[customerInGameName]    ${DEMO_IGN}
    Should Be Equal    ${order}[customerFriendId]    ${FRIEND_ID}
    Should Be Equal    ${order}[orderStatus]    PENDING
    Length Should Be    ${order}[items]    1
    Should Be Equal As Integers    ${order}[items][0][inventoryId]    ${PLAN}[inventoryId]
    Should Be Equal As Integers    ${order}[items][0][quantity]    1
    Should Be Equal As Numbers    ${order}[totalAmount]    ${PLAN}[price]    precision=2
    ${stock}=    Demo Data    stock    inventoryId=${PLAN}[inventoryId]
    ${expected_stock}=    Evaluate    int($PLAN['quantityBefore']) - 1
    Should Be Equal As Integers    ${stock}    ${expected_stock}
    ${expected_discount}=    Evaluate    round(float($order['totalAmount']) * float($TIER['rate']), 2)
    Should Be Equal As Numbers    ${order}[discountAmount]    ${expected_discount}    precision=2
    ${expected_final}=    Evaluate    round(float($order['totalAmount']) - float($order['discountAmount']), 2)
    Should Be Equal As Numbers    ${order}[finalAmount]    ${expected_final}    precision=2
    Switch Page    ${CUSTOMER_PAGE}
    Cue    04 จองแล้ว ยอดสุทธิและรหัสออเดอร์    20
    Go To    ${BASE_URL}/my-orders
    ${customer_row}=    Set Variable    tbody tr:has-text("${ORDER_CODE}")
    Wait For Elements State    ${customer_row}    visible
    Get Attribute    ${customer_row}    data-order-status    ==    PENDING
    Take Screenshot    customer-pending

    Switch Page    ${SELLER_PAGE}
    Go To    ${BASE_URL}/orders
    Focus Demo Order
    Cue    05 ร้านตรวจยอดโอนแล้ว    18
    Change Order Through UI    pay    PAID
    Click    button[data-order-id="${ORDER_ID}"][onclick^="openTradeModal"]
    Wait For Elements State    \#tradeItemsTableBody select    visible
    Cue    06 เลือกบัญชีที่พร้อมเทรด    20
    Click    \#btnAutoMatch
    Wait Until Keyword Succeeds    ${STEP_TIMEOUT}    1s    Assert Assigned
    Wait For Elements State    \#tradeItemsTableBody button[onclick*="TRADE_SENT"]    visible
    Take Screenshot    seller-assigned
    Click    \#tradeFulfillmentModal .modal-footer button
    Change Order Through UI    ship    SHIPPING
    Focus Demo Order
    Click    button[data-order-id="${ORDER_ID}"][onclick^="openTradeModal"]
    Wait For Elements State    \#tradeItemsTableBody button[onclick*="TRADE_SENT"]    visible
    Cue    07 จำลองว่าส่งการ์ดในเกมแล้ว    20
    Click    \#tradeItemsTableBody button[onclick*="TRADE_SENT"]
    Wait Until Keyword Succeeds    ${STEP_TIMEOUT}    1s    Assert Item State    TRADE_SENT
    Wait For Elements State    \#tradeItemsTableBody button[onclick*="COMPLETED"]    visible
    Cue    08 ลูกค้าได้รับครบแล้ว    15
    Click    \#tradeItemsTableBody button[onclick*="COMPLETED"]
    Wait Until Keyword Succeeds    ${STEP_TIMEOUT}    1s    Assert Completed
    # Final item closes the order. Never click the order-level complete button again.
    Click    \#tradeFulfillmentModal .modal-footer button
    Go To    ${BASE_URL}/orders
    Focus Demo Order
    Get Attribute    tbody tr:has(button[data-order-id="${ORDER_ID}"])    data-order-status    ==    COMPLETED
    Take Screenshot    seller-completed
    Switch Page    ${CUSTOMER_PAGE}
    Go To    ${BASE_URL}/my-orders
    Wait For Elements State    ${customer_row}    visible
    Scroll To Element    ${customer_row}
    Get Attribute    ${customer_row}    data-order-status    ==    COMPLETED
    Get Text    ${customer_row} .order-next-step    contains    ส่งการ์ดครบแล้ว
    Take Screenshot    customer-completed
    Cue    09 ลูกค้าตรวจผลของออเดอร์เดิม    15

*** Keywords ***
Prepare Two Sessions
    Should Be True    $MODE in ['manual', 'timed', 'fast']    MODE must be manual, timed or fast.
    Should Be True    float($SPEED) > 0    SPEED must be greater than zero.
    ${js}=    Get File    ${CURDIR}/demo.js    encoding=UTF-8
    Set Suite Variable    ${DEMO_JS}    ${js}
    ${stamp}=    Get Time    epoch
    Set Suite Variable    ${DEMO_IGN}    Demo-${stamp}
    Set Browser Timeout    ${STEP_TIMEOUT}
    Open Demo Browser
    New Demo Context    customer
    ${page}=    New Page    ${BASE_URL}/login
    Set Suite Variable    ${CUSTOMER_PAGE}    ${page}
    Sign In    ${CUSTOMER}    POKEV_DEMO_CUSTOMER_PASSWORD
    Go To    ${BASE_URL}/cards
    Get Text    .nav-user-name strong    ==    ${CUSTOMER}
    ${id}=    Get Attribute    \#customerOrderUserId    value
    Should Not Be Empty    ${id}
    Set Suite Variable    ${CUSTOMER_ID}    ${id}
    Open Demo Browser
    New Demo Context    seller
    ${page}=    New Page    ${BASE_URL}/login
    Set Suite Variable    ${SELLER_PAGE}    ${page}
    Sign In    ${SELLER}    POKEV_DEMO_SELLER_PASSWORD
    Go To    ${BASE_URL}/accounts
    Get Text    .nav-user-name strong    ==    ${SELLER}
    ${tier}=    Demo Data    tier    userId=${CUSTOMER_ID}
    Set Suite Variable    ${TIER}    ${tier}
    Go To    ${BASE_URL}/cards
    ${plan}=    Demo Data    plan    cardName=${CARD_NAME}
    Set Suite Variable    ${PLAN}    ${plan}
    Log To Console    READY: ${PLAN}[cardName], stock=${PLAN}[quantityBefore], tier=${TIER}[tier]. No order created yet.
    Switch Page    ${CUSTOMER_PAGE}

New Demo Context
    [Arguments]    ${role}
    ${viewport}=    Create Dictionary    width=${1920}    height=${1080}
    ${context_viewport}=    Set Variable    ${viewport}
    IF    ${FULLSCREEN}
        ${context_viewport}=    Set Variable    ${None}
    END
    IF    ${RECORD_VIDEO}
        ${video}=    Create Dictionary    dir=${OUTPUTDIR}/videos/${role}    size=${viewport}
        New Context    viewport=${context_viewport}    recordVideo=${video}
    ELSE
        New Context    viewport=${context_viewport}
    END

Open Demo Browser
    IF    ${FULLSCREEN}
        ${args}=    Create List    --start-fullscreen
        New Browser    chromium    headless=${HEADLESS}    args=${args}
    ELSE
        New Browser    chromium    headless=${HEADLESS}
    END

Sign In
    [Arguments]    ${username}    ${password_env}
    # Render Free can show a wake-up page before serving the real login form.
    Wait For Elements State    \#username    visible    timeout=${LOGIN_TIMEOUT}
    Fill Text    \#username    ${username}
    Fill Secret    \#password    %${password_env}
    Click    \#loginForm button[type="submit"]
    Wait For Elements State    .nav-user-name strong    visible

Demo Data
    [Arguments]    ${action}    &{args}
    ${payload}=    Create Dictionary    action=${action}    &{args}
    ${result}=    Evaluate JavaScript    ${None}    ${DEMO_JS}    arg=${payload}
    RETURN    ${result}

Cue
    [Arguments]    ${label}    ${seconds}
    Log To Console    ${label} — see doc/slide/demo-two-perspectives-script.md
    IF    $MODE == 'manual'
        Demo Data    cue    label=${label}
        Wait For Function    () => window.__demoContinue === true    timeout=${CUE_TIMEOUT}
    ELSE IF    $MODE == 'timed'
        ${duration}=    Evaluate    float($seconds) / float($SPEED)
        Sleep    ${duration}s
    END

Focus Demo Order
    Fill Text    \#orderSearch    ${ORDER_CODE}
    Wait For Elements State    button[data-order-id="${ORDER_ID}"][onclick^="openTradeModal"]    visible
    Scroll To Element    button[data-order-id="${ORDER_ID}"][onclick^="openTradeModal"]

Change Order Through UI
    [Arguments]    ${action}    ${expected}
    Click    button[data-order-id="${ORDER_ID}"][onclick*="'${action}'"]
    Click    \#transitionConfirmBtn
    Wait Until Keyword Succeeds    ${STEP_TIMEOUT}    1s    Assert Order State    ${expected}
    # Wait for the scheduled reload, not just the old modal closing.
    Wait For Elements State    tbody tr[data-order-status="${expected}"]:has(button[data-order-id="${ORDER_ID}"])    visible
    Wait For Elements State    \#transitionModal    hidden
    Focus Demo Order

Assert Order State
    [Arguments]    ${expected}
    ${order}=    Demo Data    order    orderId=${ORDER_ID}
    Should Be Equal    ${order}[orderStatus]    ${expected}
    RETURN    ${order}

Assert Assigned
    ${order}=    Assert Order State    PAID
    Should Not Be Equal    ${order}[items][0][assignedAccountId]    ${None}
    Should Be Equal    ${order}[items][0][tradeStatus]    FRIEND_PENDING

Assert Item State
    [Arguments]    ${expected}
    ${order}=    Demo Data    order    orderId=${ORDER_ID}
    Should Be Equal    ${order}[items][0][tradeStatus]    ${expected}

Assert Completed
    ${order}=    Assert Order State    COMPLETED
    Should Be Equal    ${order}[items][0][tradeStatus]    COMPLETED

Finish Demo
    IF    $TEST_STATUS == 'FAIL'
        Run Keyword And Ignore Error    Take Screenshot    demo-failure
        Log To Console    STOPPED. Check the latest order in /my-orders or /orders before rerunning. No rollback or rebooking was performed.
    END
    IF    $ORDER_ID is not None
        Log To Console    Demo order ${ORDER_CODE} / id=${ORDER_ID}; stock was consumed by one real booking.
    END

Finish Sessions
    IF    $SUITE_STATUS == 'FAIL'
        Run Keyword And Ignore Error    Take Screenshot    session-failure
    END
    Close Browser    ALL
