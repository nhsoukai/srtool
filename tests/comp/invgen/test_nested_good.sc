void main()
{

  int i;
  int j;
  int k;
  int l;

  i = j;

  while(i < 100)
  {
    i = i + 1;
    j = j + 1;
    k = 0;
    l = 0;
    while(k < 200)
    {
      k = k + 1;
      l = l + 1;
    }
    i = i + k;
    j = j + l;
  }
  assert(i == j);

}

