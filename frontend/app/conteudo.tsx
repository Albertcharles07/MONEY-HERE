import React, {useEffect, useState} from 'react';
import {Pressable, View} from 'react-native';
import {router, useLocalSearchParams} from 'expo-router';
import {Btn, Card, Grid, Input, Screen, Tile, Txt} from '../components/UI';
import {api} from '../lib/api';
import {articleSummary, fallbackContent, normalizeSearch, type Article} from '../lib/catalog';
import {useColors} from '../lib/session';

export default function Content() {
  const colors = useColors();
  const {categoria} = useLocalSearchParams<{categoria?: string}>();
  const [articles, setArticles] = useState<Article[]>(fallbackContent);
  const [search, setSearch] = useState('');

  useEffect(() => {
    let active = true;
    api('/public/content', {}, true).then((result: Article[]) => {
      if (!active) return;
      const publishedKeys = new Set(result.map(article => article.key));
      setArticles([...result, ...fallbackContent.filter(article => !publishedKeys.has(article.key))]);
    }).catch(() => {
      // O catálogo incluído no app continua disponível sem a API.
    });
    return () => { active = false; };
  }, []);

  const categories = ['Todos', ...new Set(articles.map(article => article.category))];
  const requestedCategory = typeof categoria === 'string' && categoria.trim() ? categoria.trim() : 'Todos';
  const categoryKey = normalizeSearch(requestedCategory);
  const selectedCategory = categories.find(name => normalizeSearch(name) === categoryKey) ?? requestedCategory;
  if (!categories.includes(selectedCategory)) categories.push(selectedCategory);
  const showAll = selectedCategory === 'Todos';
  const query = normalizeSearch(search);
  const visible = articles.filter(article =>
    (showAll || normalizeSearch(article.category) === categoryKey) &&
    normalizeSearch([article.title, article.category, articleSummary(article.body)].join(' ')).includes(query));

  return (
    <Screen title="Aprenda finanças">
      <Txt muted>Explore guias com exemplos do dia a dia e exercícios para praticar.</Txt>
      <Input label="Buscar conteúdo" value={search} onChangeText={setSearch} placeholder="Ex.: orçamento, cartão ou inflação" />
      <View style={{flexDirection: 'row', flexWrap: 'wrap', gap: 8}}>
        {categories.map(name => (
          <Pressable key={name} accessibilityRole="button" accessibilityState={{selected: name === selectedCategory}}
            onPress={() => router.setParams({categoria: name === 'Todos' ? '' : name})}
            style={{paddingHorizontal: 13, paddingVertical: 6, borderRadius: 18, borderWidth: 1,
              borderColor: name === selectedCategory ? colors.primary : colors.border, backgroundColor: colors.panel}}>
            <Txt size={14} bold={name === selectedCategory}>{name}</Txt>
          </Pressable>
        ))}
      </View>
      <Txt muted size={14}>{visible.length} {visible.length === 1 ? 'artigo' : 'artigos'}</Txt>
      {visible.length > 0 ? (
        <Grid>
          {visible.map(article => (
            <Tile key={article.key}>
              <Card style={{flex: 1}}>
                <View style={{flex: 1, gap: 6}}>
                  <Txt muted size={13}>{article.category} · {article.level} · {article.minutes} min</Txt>
                  <Txt bold size={19}>{article.title}</Txt>
                  <Txt size={15}>{articleSummary(article.body)}</Txt>
                </View>
                <Btn title="Ler artigo" onPress={() => router.push({
                  pathname: '/artigo/[key]',
                  params: showAll ? {key: article.key} : {key: article.key, categoria: selectedCategory},
                })} />
              </Card>
            </Tile>
          ))}
        </Grid>
      ) : <Card><Txt>{query || showAll
        ? 'Nenhum artigo encontrado. Tente outro termo ou categoria.'
        : `Ainda não há artigos em ${selectedCategory}.`}</Txt></Card>}
    </Screen>
  );
}
