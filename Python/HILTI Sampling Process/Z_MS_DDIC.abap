*&---------------------------------------------------------------------*
*& Report Z_MS_QUERIES
*&---------------------------------------------------------------------*
*&
*&---------------------------------------------------------------------*
REPORT z_ms_ddic.

" meta data
TYPES BEGIN OF ty_ddic.
INCLUDE TYPE dd03l.
TYPES text_s TYPE scrtext_s.
TYPES text_m TYPE scrtext_m.
TYPES text_l TYPE scrtext_l.
TYPES END OF ty_ddic.

TYPES : begin of line,
tname type tabname,
end of line.

data itab type line occurs 10 with header line.

itab-tname = 'MARA'.
append itab.
itab-tname = 'T179'.
append itab.
itab-tname = 'T006'.
append itab.
itab-tname = 'T023'.
append itab.
itab-tname = 'V137'.
append itab.

DATA:

  ddic   TYPE ty_ddic,

  t_ddic TYPE STANDARD TABLE OF ty_ddic.




LOOP AT itab.


SELECT  f~*,
        t~scrtext_s AS text_s,
        t~scrtext_m AS text_m,
        t~scrtext_l AS text_l
    FROM dd03l AS f
    LEFT OUTER JOIN dd04t AS t
      ON  f~tabname EQ @itab-tname
      AND f~rollname EQ t~rollname
      AND t~ddlanguage EQ 'E'
    INTO TABLE @t_ddic
    WHERE tabname = @itab-tname
  ORDER BY f~position.


LOOP AT t_ddic INTO ddic.

  WRITE /.

  WRITE

  itab-tname && ';' &&

  ddic-fieldname && ';' &&

  ddic-checktable && ';' &&
  ddic-inttype && ';' &&
  ddic-leng    && ';' &&
  ddic-decimals && ';' &&
  ddic-text_m && ';'.
ENDLOOP.


ENDLOOP.




"perform testClass.


"perform dynamicoccurence using 'VBAP'.