-- Update version 2 db to version 3

begin transaction;

drop table document_queue_relation;
drop table queues;

do $$ begin
  alter table document
    add column queue text not null default 'general' ;
exception
  when duplicate_object then null;
end $$;



end transaction;
