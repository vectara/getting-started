"""Data objects for corpus module. """
import dataclasses


@dataclasses.dataclass(frozen=True)
class Corpus:
    """Basic Corpus data."""
    corpus_id: int
    name: str
    description: str
    dt_provisioned: str
    enabled: bool


@dataclasses.dataclass(frozen=True)
class CorpusSize:
    """Corpus Size information"""
    epoch_secs: int
    size: int


@dataclasses.dataclass(frozen=True)
class ApiKey:
    """API Key information"""
    api_key: str
    description: str
    key_type: str
    enabled: bool


@dataclasses.dataclass(frozen=True)
class CorpusInfo:
    """All corpus data such as id, name, size, associated api keys etc."""
    corpus: Corpus
    status: str
    size: CorpusSize
    size_status: str
    api_keys: list[ApiKey]
    api_keys_status: str
