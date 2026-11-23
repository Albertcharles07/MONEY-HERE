import React, {useState} from 'react';
import {Screen, Card, Input, Btn, Txt, ErrorText} from '../components/UI';
import {api} from '../lib/api';

export default function Forgot() {
  const [email, setEmail] = useState('');
  const [feedback, setFeedback] = useState<{message: string; error: boolean}>();
  const [busy, setBusy] = useState(false);

  async function requestLink() {
    setFeedback(undefined);
    setBusy(true);
    try {
      const result = await api('/auth/forgot', {
        method: 'POST',
        body: JSON.stringify({email: email.trim()}),
      }, true);
      setFeedback({message: result.message, error: false});
    } catch (failure) {
      setFeedback({
        message: failure instanceof Error ? failure.message : 'Não foi possível solicitar o link de recuperação.',
        error: true,
      });
    } finally {
      setBusy(false);
    }
  }

  return (
    <Screen title="Recuperar senha">
      <Card style={{maxWidth: 480}}>
        <Input label="Seu e-mail" value={email} keyboardType="email-address" onChangeText={value => {
          setEmail(value);
          setFeedback(undefined);
        }} />
        {feedback && (feedback.error ? <ErrorText message={feedback.message} /> : <Txt>{feedback.message}</Txt>)}
        <Btn title={busy ? 'Solicitando…' : 'Enviar link'} disabled={busy || !email.trim()} onPress={requestLink} />
      </Card>
    </Screen>
  );
}
