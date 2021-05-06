const claimerSeed = '';

describe('some suite', () => {

    it('start auction', async () => {
        const invokeTx1 = invokeScript({
            dApp: "3P8gsxa1xt1xSjDP4HR728y7o9QQginK2eU",
            fee: 500000,
            call: {
                function: "initAuction",
                args: [{"type": "integer", "value": 1}, {"type": "integer", "value": 2}]
            },
            payment: [{
                assetId: "Hx9U1ossuJsoRsZmQkSKcKQZkWPmSUeeVTMQuVntwJd3",
                amount: 1
            }]
        }, claimerSeed);
        await broadcast(invokeTx1, "https://nodes.wavesnodes.com");
        console.log('Transaction:', invokeTx1);
        await waitForTx(invokeTx1.id);
        console.log('Transaction is in the blockchain: ', invokeTx1.id);
    })
})
