function compound(initial, monthly, months, monthlyRate){
 if([initial,monthly,months,monthlyRate].some(x=>!Number.isFinite(x))||initial<0||monthly<0||months<0||months>1200||monthlyRate<=-1)throw Error('Valores inválidos');
 let total=initial, invested=initial, points=[{month:0,total}];
 for(let i=1;i<=Math.floor(months);i++){total=total*(1+monthlyRate)+monthly;invested+=monthly;points.push({month:i,total});}
 return {total,invested,earnings:total-invested,points};
}
function emergency(monthlyCosts,months,already=0){if(monthlyCosts<0||months<0||already<0)throw Error('Valores inválidos');const target=monthlyCosts*months;return {target,remaining:Math.max(0,target-already)};}
function debt(balance,annualRate,monthlyPayment){if(balance<=0||annualRate<0||monthlyPayment<=0)throw Error('Valores inválidos');let remaining=balance,interest=0,periods=0;const rate=annualRate/1200;
 while(remaining>0.005&&periods<600){const fee=remaining*rate;if(monthlyPayment<=fee)throw Error('Parcela não cobre os juros');interest+=fee;remaining=Math.max(0,remaining+fee-monthlyPayment);periods++;}
 if(periods>=600&&remaining>0.005)throw Error('Prazo superior a 50 anos');return {periods,interest,total:balance+interest};}
function retirement(target,initial,years,annualRate){const months=Math.floor(years*12),r=annualRate/1200;if(months<=0||target<0||initial<0||r<=-1)throw Error('Valores inválidos');const factor=Math.pow(1+r,months);return Math.max(0,(target-initial*factor)/(r===0?months:(factor-1)/r));}
function credit(price,installments,installment){if(price<=0||installments<=0||installment<=0)throw Error('Valores inválidos');return {total:installments*installment,extra:installments*installment-price};}
module.exports={compound,emergency,debt,retirement,credit};
